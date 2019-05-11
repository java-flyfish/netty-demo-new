package com.rpc.proxy;

import com.rpc.bean.ClassInfo;
import com.rpc.config.ConfigurationFactory;
import com.rpc.config.DubboConfiguration;
import com.rpc.handers.ResultHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyRPCProxy<T> {
    private ConfigurationFactory factory = new ConfigurationFactory();

    private DubboConfiguration configuration;
    private ZkClient zkClient = null;
    String CONNECT_ADDR = "10.0.4.165:2181";
    /** session超时时间 */
    int SESSION_OUTTIME = 10000;//ms

    Map<String,Map<String,Object>> beanListMap = new ConcurrentHashMap<>();

    public NettyRPCProxy(){
//        configuration = factory.analysisXML(DubboConfiguration.class);
        this.zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR), SESSION_OUTTIME);
        System.out.println("zkClient初始化完成。。。");
    }

    public T getBean(Class target) {
        Map<String,Object> beans = beanListMap.get(target.getName());
        T t = null;
        if (beans == null || beans.isEmpty()){
            beans = new ConcurrentHashMap<>();
            t = (T)this.create(target);
            beans.put(t);
            beanListMap.put(target.getName(),beans);
        }else {
            t = (T)beans.get(0);
        }
        return t;
    }

    //根据接口创建代理对象
    public Object create(Class target,String serverAddr){
        return Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //封装 ClassInfo
                ClassInfo classInfo = new ClassInfo();
                classInfo.setClassName(target.getName());
                classInfo.setMethodName(method.getName());
                classInfo.setObjects(args);
                classInfo.setTypes(method.getParameterTypes());

                //开始使用netty发送数据
                EventLoopGroup group = new NioEventLoopGroup();
                ResultHandler resultHandler = new ResultHandler();
                try{
                    Bootstrap b = new Bootstrap();
                    b.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    //编码器
                                    pipeline.addLast("encoder", new ObjectEncoder());
                                    //解码器
                                    pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,
                                            ClassResolvers.cacheDisabled(null)));
                                    //客户端业务处理类
                                    pipeline.addLast("handler", resultHandler);
                                }
                            });
                    List<String> address = zkClient.getChildren("/serverList");
                    if (!address.isEmpty()){
                        String addr = address.get(0).split(":")[0];
                        Integer port = Integer.valueOf(address.get(0).split(":")[1]);
//                        ChannelFuture future = b.connect(configuration.getAddress(), Integer.valueOf(configuration.getPort())).sync();
                        ChannelFuture future = b.connect(addr, port).sync();
                        future.channel().writeAndFlush(classInfo).sync();
                        future.channel().closeFuture().sync();
                    }

                }finally {
                    group.shutdownGracefully();
                }
                return resultHandler.getResponse();
            }
        });
    }
}
