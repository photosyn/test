package com.bitselink.Client;

import com.bitselink.ICallBack;
import com.bitselink.config.Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import java.util.concurrent.TimeUnit;

public class EchoClient {
    public ICallBack callBackObject = null;// 引用回调对象
    private final static String HOST = "192.168.3.4";
    private final static int PORT = 8089;
    private static int msgId = 0;
    private Channel channel;
    Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup();

    public EchoClient(ICallBack obj){
        this.callBackObject = obj;
    }

    public void sendParkingData(String str){
        if (channel != null && channel.isActive()) {
            msgId++;
            String packStr = "{ \"msgid\":123,\"msgtype\":\"parkingData\"" + ",\"data\":" + str + "}\r\n";
            channel.writeAndFlush(Unpooled.copiedBuffer(packStr, CharsetUtil.UTF_8));
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

        try {
            ChannelFuture future = bootstrap.connect(Config.rootConfig.cloud.ip, port).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture futureListener) throws Exception {
                    if (futureListener.isSuccess()){
                        channel = futureListener.channel();
                        callBackObject.setCloudConnected(true);
                        System.out.println("Connect to server successfully!");
                    } else {
                        callBackObject.setCloudConnected(false);
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
        } catch (Exception e){
            e.printStackTrace();
        }

//        future.channel().closeFuture().sync();
    }
}
