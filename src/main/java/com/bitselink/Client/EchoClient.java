package com.bitselink.Client;

import com.bitselink.Client.Protocol.*;
import com.bitselink.ICallBack;
import com.bitselink.LogHelper;
import com.bitselink.config.Config;
import com.bitselink.domain.ParkingGroupData;
import com.bitselink.helloform;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EchoClient {
    public ICallBack callBackObject;// 引用回调对象
    private Channel channel;
    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup();

    private BitLinkEncoder encoder;
    private RegisterData registerData;
    private HeartbeatData heartbeatData;
    private List<DiagnosisData> diagnosisHistory;
    private VersionData versionData;
    private UpgradeData upgradeData;
    private UploadConfigData uploadConfigData;
    private RetcodeData downloadConfigData;

    public EchoClient(ICallBack obj){
        this.callBackObject = obj;
        encoder = new BitLinkEncoder();

        versionData = new VersionData();
        RespHead versionHead = new RespHead();
        versionHead.setMcode(MCodeType.M_CODE_TYPE_VER_INFO.getMsg());
        versionHead.setVer(MsgHead.VER);
        versionHead.setMsgatr(MsgHead.HEAD_PC_RESPOND);
        versionHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        versionHead.setRcode(RespHead.RESPHEAD_RCODE_OK);
        VersionBody versionBody = new VersionBody();
        versionBody.setAppver(helloform.SOFT_VER);
        versionData.getHead().add(versionHead);
        versionData.getBody().add(versionBody);

        upgradeData = new UpgradeData();
        RespHead upgradeHead = new RespHead();
        upgradeHead.setMcode(MCodeType.M_CODE_TYPE_UPGRADE.getMsg());
        upgradeHead.setVer(MsgHead.VER);
        upgradeHead.setMsgatr(MsgHead.HEAD_PC_RESPOND);
        upgradeHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        upgradeHead.setRcode(RespHead.RESPHEAD_RCODE_OK);
        UpgradeBody upgradeBody = new UpgradeBody();
//        upgradeBody.setAppver(helloform.SOFT_VER);
        upgradeData.getHead().add(upgradeHead);
        upgradeData.getBody().add(upgradeBody);

        uploadConfigData = new UploadConfigData();
        RespHead uploadConfigHead = new RespHead();
        uploadConfigHead.setMcode(MCodeType.M_CODE_TYPE_UPLOAD_CONFIG.getMsg());
        uploadConfigHead.setVer(MsgHead.VER);
        uploadConfigHead.setMsgatr(MsgHead.HEAD_PC_RESPOND);
        uploadConfigHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        uploadConfigHead.setRcode(RespHead.RESPHEAD_RCODE_OK);
        UploadConfigBody uploadConfigBody = new UploadConfigBody();
        uploadConfigData.getHead().add(uploadConfigHead);
        uploadConfigData.getBody().add(uploadConfigBody);

        downloadConfigData = new RetcodeData();
        RespHead downloadConfigHead = new RespHead();
        downloadConfigHead.setMcode(MCodeType.M_CODE_TYPE_DOWNLOAD_CONFIG.getMsg());
        downloadConfigHead.setVer(MsgHead.VER);
        downloadConfigHead.setMsgatr(MsgHead.HEAD_PC_RESPOND);
        downloadConfigHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        downloadConfigHead.setRcode(RespHead.RESPHEAD_RCODE_OK);
        RetcodeBody downloadConfigBody = new RetcodeBody();
        downloadConfigData.getHead().add(downloadConfigHead);
        downloadConfigData.getBody().add(downloadConfigBody);


        registerData = new RegisterData();
        MsgHead registerHead = new MsgHead();
        registerHead.setMcode(MCodeType.M_CODE_TYPE_REGESTER.getMsg());
        registerHead.setVer(MsgHead.VER);
        registerHead.setMsgatr(MsgHead.HEAD_REQUEST);
        registerHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        RegisterBody registerBody = new RegisterBody();
        registerData.getHead().add(registerHead);
        registerData.getBody().add(registerBody);

        heartbeatData = new HeartbeatData();
        MsgHead heartbeatHead = new MsgHead();
        heartbeatHead.setMcode(MCodeType.M_CODE_TYPE_HEART_BEAT.getMsg());
        heartbeatHead.setVer(MsgHead.VER);
        heartbeatHead.setMsgatr(MsgHead.HEAD_HEART);
        heartbeatHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        HeartbeatBody heartbeatBody = new HeartbeatBody();
        heartbeatBody.setSerial(Config.rootConfig.register);
        heartbeatData.getHead().add(heartbeatHead);
        heartbeatData.getBody().add(heartbeatBody);

        diagnosisHistory = new ArrayList<>();

        Config.setIsWaitRegister(true);
    }

    public Channel getChannel() {
        return channel;
    }

    public void sendHeartbeat(Channel channel) {
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            heartbeatData.getHead().get(0).generateIdAndTime();
            heartbeatData.getBody().get(0).setSerial(Config.rootConfig.register);
            String sendStr = encoder.encode(heartbeatData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void sendRegisterData(Channel channel) {
        if (channel != null && channel.isActive()) {
            registerData.getHead().get(0).generateIdAndTime();
            registerData.getBody().get(0).setTelno(Config.rootConfig.cloud.phone);
            registerData.getBody().get(0).setDevno(Config.rootConfig.register);
            String sendStr = encoder.encode(registerData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void sendParkingData(ParkingGroupData parkingGroupData){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            if (parkingGroupData.getBody().size() > 0)
            {
                parkingGroupData.getHead().get(0).generateIdAndTime();
                String sendStr = encoder.encode(parkingGroupData);
                channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
            }
        }
    }

    public void addDiagnosisData(String diagnosisInfo, int level) {
        DiagnosisData diagnosisData = new DiagnosisData();
        MsgHead diagnosisHead = new MsgHead();
        diagnosisHead.setMcode(MCodeType.M_CODE_TYPE_DIAGNOSIS.getMsg());
        diagnosisHead.setVer(MsgHead.VER);
        diagnosisHead.setMsgatr(MsgHead.HEAD_REQUEST);
        diagnosisHead.setSafeflg(MsgHead.SAFEFLAG_ALL);
        DiagnosisBody diagnosisBody = new DiagnosisBody();
        diagnosisBody.setDevno(Config.rootConfig.register);
        diagnosisBody.setErrorlevel(level);
        diagnosisData.getHead().add(diagnosisHead);
        diagnosisData.getBody().add(diagnosisBody);
        if (diagnosisData.getBody().size() > 0)
        {
            diagnosisData.getHead().get(0).generateIdAndTime();
            diagnosisData.getBody().get(0).generateTime();
            diagnosisData.getBody().get(0).setDevno(Config.rootConfig.register);
            diagnosisData.getBody().get(0).setErrormsg(diagnosisInfo);
        }
        diagnosisHistory.add(diagnosisData);
    }

    public void sendDiagnosisData(){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            if(diagnosisHistory.size() > 0)
            {
                DiagnosisData diagnosisData = diagnosisHistory.remove(0);
                String sendStr = encoder.encode(diagnosisData);
                channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
            }
        }
    }

    public void sendVersionData(){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            versionData.getHead().get(0).generateIdAndTime();
            String sendStr = encoder.encode(versionData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void sendUpgradeData(String filename, int position, String version, int retcode){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            upgradeData.getBody().get(0).setFile(filename);
            upgradeData.getBody().get(0).setStart(position);
            upgradeData.getBody().get(0).setRetcode(retcode);
            upgradeData.getBody().get(0).setVersion(version);
            upgradeData.getBody().get(0).setDevno(Config.rootConfig.register);
            String sendStr = encoder.encode(upgradeData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void sendConfigData(){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            uploadConfigData.getBody().get(0).setDevno(Config.rootConfig.register);
            try {
                String context = FileUtils.readFileToString(new File("sites.conf.json"), "utf8");
                uploadConfigData.getBody().get(0).setConfig(context);
            } catch (FileNotFoundException e) {
                LogHelper.warn("找不到文件：" + e.getMessage());
            } catch (IOException e) {
                LogHelper.warn("IO操作异常：" + e.getMessage());
            }
//            uploadConfigData.getBody().get(0).setFilesize();
            String sendStr = encoder.encode(uploadConfigData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void sendConfigDownloadResult(int retcode, String remark){
        if (channel != null && channel.isActive() && !Config.rootConfig.register.isEmpty()) {
            downloadConfigData.getBody().get(0).setDevno(Config.rootConfig.register);
            downloadConfigData.getBody().get(0).setRetcode(retcode);
            downloadConfigData.getBody().get(0).setRemark(remark);
            String sendStr = encoder.encode(downloadConfigData);
            channel.writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
        }
    }

    public void start(){
        try{
            bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("logging", new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(20,0,0));
                            ch.pipeline().addLast(new EchoClientHandler(EchoClient.this));
                        }
                    });
            doConnect();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected void doConnect(){
        if (channel != null && channel.isActive()) {
            return;
        }
        if (Config.rootConfig.cloud.ip.isEmpty()){
            return;
        }
        int port = Integer.parseInt(Config.rootConfig.cloud.port);
        if (port <= 0){
            return;
        }

        LogHelper.info("连接中心服务器：ip=" + Config.rootConfig.cloud.ip + ", port=" + port);
        ChannelFuture future = bootstrap.connect(Config.rootConfig.cloud.ip, port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()){
                    channel = futureListener.channel();
                    LogHelper.warn("中心服务器：" + channel.remoteAddress() + "连接成功");
                    if (Config.rootConfig.register.isEmpty()) {
                        callBackObject.setCloudState(CloudState.NO_REGISTERED,"");
                        LogHelper.warn("设备未注册");
                    }
                } else {
                    callBackObject.setCloudState(CloudState.CONNECT_FAIL, "");
                    LogHelper.warn("中心服务器连接失败，10秒后重新尝试");
                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });

//        future.channel().closeFuture().sync();
    }
}
