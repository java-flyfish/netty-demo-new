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
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NettyRPCProxy<T> implements ApplicationListener<ContextRefreshedEvent> {
    private ConfigurationFactory factory = new ConfigurationFactory();

    String zkRoot = "/serverList";
    Class aClass;
    private DubboConfiguration configuration;
    private ZkClient zkClient = null;
    String CONNECT_ADDR = "192.168.1.9:2181";
    //session超时时间 ms
    int SESSION_OUTTIME = 10000;

    Map<String,T> beanListMap = new ConcurrentHashMap<>();

    public NettyRPCProxy(Class aClass){
        this.aClass = aClass;
//        configuration = factory.analysisXML(DubboConfiguration.class);
        this.zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR), SESSION_OUTTIME);
        System.out.println("zkClient初始化完成。。。");
    }

    //对外提供的获取bean的方法
    public T getBean() {
        if (beanListMap.isEmpty()){
            System.out.println("未找到服务！");
            return null;
        }
        Set<Map.Entry<String, T>> beans = beanListMap.entrySet();
        Random random = new Random();
        int index = random.nextInt(beans.size() - 1);
        List<Object> beanList = Arrays.asList(beans.toArray());
        T t = (T)beanList.get(index);
        return t;
    }

    //监听节点变化
    public void serverNodeChangeEvent(){
        zkClient.subscribeChildChanges(zkRoot, new IZkChildListener() {
			@Override
			public void handleChildChange(String s, List<String> list) throws Exception {
				System.out.println("参数s：" + s);
				System.out.println("参数list：" + list);
			}
		});
    }

    //初始化时创建bean
    public void createBean(){
        List<String> address = zkClient.getChildren(zkRoot);
        for (String addr : address){
            Object bean = this.doCreate(aClass, addr);
            if (bean != null){
                beanListMap.put(addr,(T)bean);
            }
        }
    }

    //根据接口创建代理对象
    public Object doCreate(Class target,String serverAddr){
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

                    String addr = serverAddr.split(":")[0];
                    Integer port = Integer.valueOf(serverAddr.split(":")[1]);
//                        ChannelFuture future = b.connect(configuration.getAddress(), Integer.valueOf(configuration.getPort())).sync();
                    ChannelFuture future = b.connect(addr, port).sync();
                    future.channel().writeAndFlush(classInfo).sync();
                    future.channel().closeFuture().sync();


                }finally {
                    group.shutdownGracefully();
                }
                return resultHandler.getResponse();
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.serverNodeChangeEvent();
        this.createBean();
    }
}
