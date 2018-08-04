package com.bitselink.Client;

import com.alibaba.fastjson.JSON;
import com.bitselink.ICallBack;
import com.bitselink.config.Config;
import com.bitselink.domain.ParkingGroupData;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import java.util.concurrent.TimeUnit;

public class EchoClient {
    public ICallBack callBackObject;// 引用回调对象
    private Channel channel;
    private Bootstrap bootstrap;
    private BitLinkEncoder encoder = new BitLinkEncoder();
    private NioEventLoopGroup workGroup = new NioEventLoopGroup();
    private EchoClientHandler clientHandler = new EchoClientHandler(EchoClient.this);

    public EchoClient(ICallBack obj){
        this.callBackObject = obj;
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

    public void sendRegisterData() {
        if (channel != null && channel.isActive()) {
            clientHandler.sendRegisterData(channel);
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
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(20,10,0));
                            ch.pipeline().addLast(clientHandler);
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

        ChannelFuture future = bootstrap.connect(Config.rootConfig.cloud.ip, port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()){
                    channel = futureListener.channel();
                    if (Config.rootConfig.register.isEmpty()) {
                        callBackObject.setCloudState(CloudState.NO_REGISTERED);
                        System.out.println("Connect to server successfully, but need register!");
                    } else {
                        callBackObject.setCloudState(CloudState.CONNECTED);
                        System.out.println("Connect to server successfully!");
                    }
                } else {
                    callBackObject.setCloudState(CloudState.CONNECT_FAIL);
                    System.out.println("Failed to connect to server, try connect after 10s");
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
