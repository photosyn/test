package com.bitselink.Client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.bitselink.Client.Protocol.*;
import com.bitselink.LogHelper;
import com.bitselink.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Base64;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private EchoClient client;
    private String leftStr;
    private int heartTimeoutCnt;

    public EchoClientHandler(EchoClient client) {
        super();
        this.client = client;
        leftStr = "";
        heartTimeoutCnt = 0;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                if (heartTimeoutCnt >= 1) {
                    ctx.close();
                }else {
                    if (Config.isIsWaitRegister()) {
                        client.sendRegisterData(ctx.channel());
                    } else {
                        client.sendHeartbeat(ctx.channel());
                    }
                    heartTimeoutCnt++;
                }

            }
//            else if (event.state() == IdleState.WRITER_IDLE) {
//                if (Config.isIsWaitRegister()) {
//                    if (heartTimeoutCnt >= 1) {
//                        ctx.close();
//                    }else {
//                        client.sendRegisterData(ctx.channel());
//                        heartTimeoutCnt++;
//                    }
//
//                }
//            }
//            else if (event.state() == IdleState.ALL_IDLE)
//                System.out.println("all idle");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!\r\n", CharsetUtil.UTF_8));
        Config.syncParamUpdate(true);
        client.callBackObject.setCloudState(CloudState.CONNECTED,"");
        LogHelper.info("中心服务器：" + ctx.channel().remoteAddress() + "连接成功");
        client.sendRegisterData(ctx.channel());
        heartTimeoutCnt = 1;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        leftStr += msg.toString(CharsetUtil.UTF_8);

        if (leftStr.length() >= 8) {
            int frameLen = Integer.parseInt(leftStr.substring(0,8));
            if (leftStr.length() >= frameLen + 8){
                String encodeStr = leftStr.substring(8,8+frameLen);
                leftStr = leftStr.substring(8+frameLen);
                leftStr = StringUtils.substringAfter(leftStr, "#$D#$A");
                String json = null;
                try {
                    json = new String(Base64.getDecoder().decode(encodeStr),"utf-8");
                    LogHelper.info("客户端接收数据:" + json);
                } catch (UnsupportedEncodingException e) {
                    LogHelper.warn("客户端接收数据decode失败：" + e.getMessage());
                    return;
                }

                RespHead respHead = null;
                try {
                    respHead = JSON.parseObject(json).getJSONArray("head").getJSONObject(0).toJavaObject(RespHead.class);
                } catch ( Exception e) {
                    LogHelper.warn("客户端转换decode数据头失败：" + e.getMessage());
                    return;
                }

                switch (MCodeType.getByValue(respHead.getMcode()))
                {
                    case M_CODE_TYPE_HEART_BEAT:
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            if(heartTimeoutCnt > 0){
                                heartTimeoutCnt--;
                            }
                            client.sendDiagnosisData();
//                            Config.syncParamUpdate(false);
                        }
                        break;
                    }
                    case M_CODE_TYPE_VER_INFO:
                    {
                        client.sendVersionData();
                        break;
                    }
                    case M_CODE_TYPE_UPGRADE:
                    {
                        boolean exceptionHappened = false;
                        String filename = "";
                        String version = "";
                        try {
                            RespUpgradeBody respUpgradeBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespUpgradeBody.class);
                            int cmdtype = respUpgradeBody.getCmdtype();
                            int cancel = respUpgradeBody.getRetcancel();
                            filename = respUpgradeBody.getFile();
                            int filesize = respUpgradeBody.getFilesize();
                            version = respUpgradeBody.getVersion();
                            String filepath = filename + "_" + version + "_" + filesize + ".tmp";
                            if(cancel == 0) {
                                if(cmdtype == 0) {
                                    LogHelper.info("启动设备升级，升级包：" + filename + "_" + version + "_" + filesize);
                                    File file = new File(filepath);
                                    int startindex = 0;
                                    if (file.exists()) {
                                        startindex = (int)file.length();
                                    }
                                    client.sendUpgradeData(filename, startindex, version, 0);
                                } else {
                                    int blocksize = respUpgradeBody.getBlocksize();
                                    String stream = respUpgradeBody.getStream();
                                    LogHelper.info("设备升级中，blocksize：" + blocksize);
                                    File file = new File(filepath);
                                    int startindex = 0;
                                    if (file.exists()) {
                                        startindex = (int)file.length();
                                    }
                                    RandomAccessFile access = new RandomAccessFile(file,"rw");
                                    access.seek(startindex);
                                    access.write(stream.getBytes());
                                    int writeindex = (int)file.length();
                                    if (writeindex >= filesize) {
                                        file.renameTo(new File("upgrade.jar"));
                                    }
                                    client.sendUpgradeData(filename, writeindex, version, 0);
                                }
                            }
                        } catch (JSONException e) {
                            LogHelper.warn("客户端转换升级body数据失败：" + e.getMessage());
                            exceptionHappened = true;
                        } catch (FileNotFoundException e) {
                            LogHelper.warn("文件不存在：" + e.getMessage());
                            exceptionHappened = true;
                        } catch (IOException e) {
                            LogHelper.warn("文件定位失败：" + e.getMessage());
                            exceptionHappened = true;
                        }
                        if (exceptionHappened) {
                            client.sendUpgradeData(filename, 0, version, 1);
                        }
                        break;
                    }
                    case M_CODE_TYPE_UPLOAD_CONFIG:
                    {
                        client.sendConfigData();
                        break;
                    }
                    case M_CODE_TYPE_DOWNLOAD_CONFIG:
                    {
                        RespDownloadConfigBody respDownloadConfigBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespDownloadConfigBody.class);
                        String stream = respDownloadConfigBody.getStream();
                        try {
                            FileWriter fw = new FileWriter("sites.conf.json");
                            fw.write(stream);
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        client.sendConfigDownloadResult();
                        break;
                    }
                    case M_CODE_TYPE_RESTART:
                    {
                        Runtime.getRuntime().addShutdownHook(new Thread() {
                            public void run() {
                                try {
                                    Runtime.getRuntime().exec("sh restart.sh");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        System.exit(0);
                        break;
                    }
                    case M_CODE_TYPE_REGESTER:
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            if(heartTimeoutCnt > 0){
                                heartTimeoutCnt--;
                            }
                            try {
                                RespRegisterBody respRegisterBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespRegisterBody.class);
                                Config.rootConfig.register = respRegisterBody.getDevno();
                                Config.save();
                                LogHelper.info("设备注册成功");
                            } catch (JSONException e) {
                                LogHelper.warn("客户端转换注册body数据失败：" + e.getMessage());
                            }
                            Config.setIsWaitRegister(false);
                        }
                        else {
                            try {
                                RespRegisterBody respRegisterBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespRegisterBody.class);
                                client.callBackObject.setCloudState(CloudState.REGISTER_FAIL,respRegisterBody.getRetmsg());
                            } catch (JSONException e) {
                                LogHelper.warn("客户端转换注册body数据失败：" + e.getMessage());
                            }
                        }
                        break;
                    }
                    case M_CODE_TYPE_PARK_DATA:
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            //接收到应答，更新查询条件
                            //如果更新查询条件的事件是旧时间，则直接启动同步发送，否者等待定时时间到达之后再启动
                            if (Config.syncParamUpdate(false)) {
                                client.callBackObject.setParkingDataRespondReceived(true);
                            }
                        }
                        break;
                    }
                    case M_CODE_TYPE_DIAGNOSIS:
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
//                            Config.syncParamUpdate(false);
                            client.sendDiagnosisData();
                        }
                        break;
                    }
                    default: {
                        LogHelper.warn("客户端收到不支持的Mcode：" + respHead.getMcode());
                        break;
                    }
                }
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        LogHelper.info(cause.getLocalizedMessage());
        LogHelper.info(cause.getStackTrace().toString());
        LogHelper.info("连接异常，关闭连接：" + cause.getMessage());
        //ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogHelper.info("通信信道：" + ctx.channel().remoteAddress() + "失效");
        super.channelInactive(ctx);
        client.doConnect();
    }

}
