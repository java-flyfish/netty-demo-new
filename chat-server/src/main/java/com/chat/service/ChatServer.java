package com.chat.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Scanner;

@Component
public class ChatServer {

    private int port;

    public void run() throws InterruptedException {
        //1.创建一个线程组:接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //2.创建一个线程中:处理网络操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //3.创建服务器端启动助手来配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //4.进行一些列的配置
            serverBootstrap.group(bossGroup,workerGroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class)//使用NioServerSocketChannel作为服务器实现
                    .option(ChannelOption.SO_BACKLOG,128)//设置线程队列中等待连接的个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true)//连接保持活动状态
                    //通过通道初始化对象,往pipeline链中加入自定义的处理类
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel sc){
                            sc.pipeline()
                                    //往pipeline链中添加解码器
                                    .addLast("decoder",new StringDecoder())
                                    //往pipeline链中添加编码器
                                    .addLast("encoder",new StringEncoder())
                                    //往pipeline链中添加自定义的业务处理类
                                    .addLast(new ChatServerHandler());
                        }
                    });
            //5.绑定端口号,非阻塞方式
            ChannelFuture cf = serverBootstrap.bind(9999).sync();
            System.out.println("..........Netty Chat Server启动...........");
            //6.异步关闭通道,关闭线程组
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @PostConstruct
    public void init() throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("请输入绑定的端口号:");
                String portStr = scan.nextLine();
                port = Integer.valueOf(portStr);
                break;
            } catch (Exception e) {
                System.out.println("您输入的端口号有误,请输入大于0的数字...");
            }
        }
        System.out.println("输入端口号："+port);
        this.run();
    }
}
