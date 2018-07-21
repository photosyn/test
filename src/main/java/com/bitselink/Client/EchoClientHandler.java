package com.bitselink.Client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bitselink.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private EchoClient client;
    public EchoClientHandler(EchoClient client) {
        super();
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!\r\n", CharsetUtil.UTF_8));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        String resp = msg.toString(CharsetUtil.UTF_8);
        System.out.println("Client received:" + msg.toString(CharsetUtil.UTF_8) + "\r\n");
        JSONObject jsonObject = JSON.parseObject(resp);
        String msgtype = jsonObject.getString("msgtype");
        if (msgtype.equals("parkingData")){
            boolean success = jsonObject.getBoolean("success");
            if (success){
                Config.syncParamUpdate(false);
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
