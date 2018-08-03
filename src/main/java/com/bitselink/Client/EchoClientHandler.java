package com.bitselink.Client;

import com.alibaba.fastjson.JSON;
import com.bitselink.Client.Protocol.*;
import com.bitselink.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private EchoClient client;
    private String leftStr;
    private RegisterData registerData;
    private HeartbeatData heartbeatData;
    private BitLinkEncoder encoder;

    public EchoClientHandler(EchoClient client) {
        super();
        this.client = client;
        leftStr = "";

        encoder = new BitLinkEncoder();

        registerData = new RegisterData();
        MsgHead registerHead = new MsgHead();
        registerHead.setMcode("100001");
        registerHead.setVer("0001");
        registerHead.setMsgatr("20");
        registerHead.setSafeflg("11");
        registerHead.setMac("");
        RegisterBody registerBody = new RegisterBody();
        registerBody.setTelno("13812345678");
        registerData.getHead().add(registerHead);
        registerData.getBody().add(registerBody);

        heartbeatData = new HeartbeatData();
        MsgHead heartbeatHead = new MsgHead();
        heartbeatHead.setMcode("000001");
        heartbeatHead.setVer("0001");
        heartbeatHead.setMsgatr("99");
        heartbeatHead.setSafeflg("11");
        heartbeatHead.setMac("");
        HeartbeatBody heartbeatBody = new HeartbeatBody();
        heartbeatBody.setSerial(Config.rootConfig.register);
        heartbeatData.getHead().add(heartbeatHead);
        heartbeatData.getBody().add(heartbeatBody);

    }

    private void sendHeartbeat(ChannelHandlerContext ctx) {
//        heartbeatData.getHead().get(0).generateIdAndTime();
//        String packStr = JSON.toJSONString(heartbeatData);
//        String encoded = "";
//        try {
//            encoded = Base64.getEncoder().encodeToString(packStr.getBytes("utf-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String sendStr = String.format("%1$08d",encoded.length()) + encoded;

        heartbeatData.getHead().get(0).generateIdAndTime();
        String sendStr = encoder.encode(heartbeatData);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
    }

    private void sendRegisterData(ChannelHandlerContext ctx) {

        registerData.getHead().get(0).generateIdAndTime();
        String sendStr = encoder.encode(registerData);

//        String packStr = JSON.toJSONString(registerData);
//        String encoded = "";
//        try {
//            encoded = Base64.getEncoder().encodeToString(packStr.getBytes("utf-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String sendStr = String.format("%1$08d",encoded.length()) + encoded;
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(sendStr, CharsetUtil.UTF_8));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE)
                sendHeartbeat(ctx);
//                System.out.println("read idle");
            else if (event.state() == IdleState.WRITER_IDLE)
                System.out.println("write idle");
            else if (event.state() == IdleState.ALL_IDLE)
                System.out.println("all idle");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!\r\n", CharsetUtil.UTF_8));
        if (Config.rootConfig.register.isEmpty()) {
            sendRegisterData(ctx);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        leftStr += msg.toString(CharsetUtil.UTF_8);
        System.out.println("Client received:" + msg.toString(CharsetUtil.UTF_8) + "\r\n");

        if (leftStr.length() >= 8) {
            int frameLen = Integer.parseInt(leftStr.substring(0,8));
            if (leftStr.length() >= frameLen + 8){
                String encodeStr = leftStr.substring(8,8+frameLen);
                leftStr = leftStr.substring(8+frameLen);
                String json = null;
                try {
                    json = new String(Base64.getDecoder().decode(encodeStr),"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                RespHead respHead = JSON.parseObject(json).getJSONArray("head").getJSONObject(0).toJavaObject(RespHead.class);

                switch (respHead.getMcode())
                {
                    case "000001":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            Config.syncParamUpdate(false);
                        }
                        break;
                    }
                    case "100001":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            RespRegisterBody respRegisterBody = JSON.parseObject(json).getJSONArray("body").getJSONObject(0).toJavaObject(RespRegisterBody.class);
                            Config.rootConfig.register = respRegisterBody.getDevno();
                            Config.save();
                        }
                        else {
                            Config.setCloudRefuse(true);
                        }
                        break;
                    }
                    case "100002":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            Config.syncParamUpdate(false);
                        }
                        break;
                    }
                    case "100003":
                    {
                        if(respHead.getRcode().equals("0000"))
                        {
                            Config.syncParamUpdate(false);
                        }
                        break;
                    }
                    default:break;
                }
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.doConnect();
    }

}
