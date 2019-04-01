package com.chat.chat.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Scanner;

@Component
public class ChatClient {
    private String host;//服务端地址
    private int port;//服务端端口号

    public void run() throws InterruptedException {
        //1.创建一个线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)//设置线程组
                    .channel(NioSocketChannel.class)//设置线程组
                    //加入处理类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel sc) {
                            sc.pipeline()
                                    //往pipeline链中添加解码器
                                    .addLast("decoder",new StringDecoder())
                                    //往pipeline链中添加编码器
                                    .addLast("encoder",new StringEncoder())
                                    //往pipeline链中添加自定义的业务处理类
                                    .addLast(new ChatClientHandler());
                        }
                    });
            System.out.println(".....client is ready......");
            //连接服务端
            ChannelFuture cf = b.connect(host,port).sync();
            Channel channel = cf.channel();
            System.out.println("------" + channel.localAddress().toString().substring(1) + "--------");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()){
                String msg = scanner.nextLine();
                channel.writeAndFlush(msg+"\r\n");
            }
        }finally {
            group.shutdownGracefully();
        }
    }

    @PostConstruct
    public void init() throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        System.out.print("请输入服务器地址:");
        host = scan.nextLine();
        while (true) {
            try {
                System.out.print("请输入服务器端口号:");
                String portStr = scan.nextLine();
                port = Integer.valueOf(portStr);
                break;
            } catch (Exception e) {
                System.out.println("您输入的端口号有误,请输入大于0的数字...");
            }
        }

        System.out.println("输入的地址和端口号：" + host + ":" +port);
        this.run();
    }
}
