package com.rpc.handlers;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyRPCServer implements ApplicationListener<ContextRefreshedEvent>,ApplicationContextAware{

    private Integer port = null;
    private ZkClient zkClient = null;

    public NettyRPCServer(){}

    public NettyRPCServer(ZkClient zkClient,Integer port){
        this.port = port;
        this.zkClient = zkClient;
    }

    private ApplicationContext applicationContext;

    public void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //编码器
                            pipeline.addLast("encoder", new ObjectEncoder());
                            //解码器
                            pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,
                                    ClassResolvers.cacheDisabled(null)));
                            //服务器端业务处理类
                            pipeline.addLast(new InvokeHandler(applicationContext));
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {

                    //注册服务
                    regiestServer();
                    System.out.println("绑定端口结束！");
                }
            });
            System.out.println("......Server is ready......");
            future.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println("处理结果结束！");
                }
            });
        }catch (Exception e){
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (port == null){
            port = 8888;
        }
        this.run();
    }

    public void destroy() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("获取当前服务器地址失败！");
            e.printStackTrace();
        }
        String regiestAddress = address.getHostAddress() + ":" + port;
        if (zkClient.exists("/serverList/" + regiestAddress)){
            zkClient.delete("/serverList/" + regiestAddress);
        }
    }

    private void regiestServer(){
        //在zk中注册本机地址
        if (zkClient == null){
            zkClient = new ZkClient("localhost:2181",10000);
        }

        if (!zkClient.exists("/serverList")){
            //节点不存在，则创建节点
            zkClient.createPersistent("/serverList");
        }
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("获取当前服务器地址失败！");
            e.printStackTrace();
        }
        String regiestAddress = address.getHostAddress() + ":" + port;
        //创建子节点
        if (!zkClient.exists("/serverList/" + regiestAddress)){
            //节点不存在，则创建节点
            zkClient.createEphemeral("/serverList/" + regiestAddress);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
