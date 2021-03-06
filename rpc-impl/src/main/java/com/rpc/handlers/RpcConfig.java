package com.rpc.handlers;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Info:
 * @ClassName: RpcConfig
 * @Author: weiyang
 * @Data: 2019/5/8 10:07 AM
 * @Version: V1.0
 **/
@Configuration
public class RpcConfig {

    //地址
    String CONNECT_ADDR = "192.168.1.9:2181";
    //session超时时间 ms
    Integer SESSION_OUTTIME = 10000;

    @Bean
    public ZkClient zkClient(){
        ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR), SESSION_OUTTIME);
        return zkClient;
    }

    @Bean(destroyMethod = "destroy")
    public NettyRPCServer nettyRPCServer(ZkClient zkClient){
        NettyRPCServer nettyRPCServer = new NettyRPCServer(zkClient,8888);
        return nettyRPCServer;
    }
}
