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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private EchoClient client;
    private String leftStr;

    public EchoClientHandler(EchoClient client) {
        super();
        this.client = client;
        leftStr = "";
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                if (!Config.isIsWaitRegister()) {
                    client.sendHeartbeat(ctx.channel());
                }
            }
            else if (event.state() == IdleState.WRITER_IDLE) {
                if (Config.isIsWaitRegister()) {
                    client.sendRegisterData(ctx.channel());
                }
            }
//            else if (event.state() == IdleState.ALL_IDLE)
//                System.out.println("all idle");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!\r\n", CharsetUtil.UTF_8));
        LogHelper.info("通信信道激活");
        if (Config.rootConfig.register.isEmpty()) {
            Config.setIsWaitRegister(true);
            client.sendRegisterData(ctx.channel());
        } else {
            Config.setIsWaitRegister(false);
            client.sendHeartbeat(ctx.channel());
        }
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

                switch (respHead.getMcode())
                {
                    case "000001":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
//                            Config.syncParamUpdate(false);
                        }
                        break;
                    }
                    case "100001":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            try {
                                RespRegisterBody respRegisterBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespRegisterBody.class);
                                Config.rootConfig.register = respRegisterBody.getDevno();
                                Config.save();
                                client.callBackObject.setCloudState(CloudState.CONNECTED);
                            } catch (JSONException e) {
                                LogHelper.warn("客户端转换注册body数据失败：" + e.getMessage());
                            }
                        }
                        Config.setIsWaitRegister(false);
                        break;
                    }
                    case "100002":
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
                    case "100003":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
//                            Config.syncParamUpdate(false);
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
        LogHelper.info("连接异常，关闭连接：" + cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogHelper.info("通信信道失效");
        super.channelInactive(ctx);
        client.doConnect();
    }

}
