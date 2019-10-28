package com.rpc;

import com.rpc.config.ConfigurationFactory;
import com.rpc.config.DubboConfiguration;
import com.rpc.proxy.NettyRPCProxy;
import com.rpc.service.HelloNetty;
import com.rpc.service.HelloRPC;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RpcClientApplicationTests {

	@Test
	public void contextLoads() {
		NettyRPCProxy<HelloNetty> proxy = new NettyRPCProxy(HelloNetty.class);
		proxy.createBean();
		HelloNetty helloNetty= proxy.getBean();
		System.out.println(helloNetty.hello());
		//第 2 次远程调用
		NettyRPCProxy<HelloRPC> proxy1 = new NettyRPCProxy(HelloRPC.class);
		proxy1.createBean();
		HelloRPC helloRPC = proxy1.getBean();
		System.out.println(helloRPC.hello("RPC"));
	}

	@Test
	public void testAnalysisXml(){
		ConfigurationFactory factory = new ConfigurationFactory();
		DubboConfiguration configuration = factory.analysisXML(DubboConfiguration.class);
		System.out.println(configuration);
	}

	@Test
	public void testZkClient(){
		String CONNECT_ADDR = "192.168.1.9:2181";
		//session超时时间 ms
		int SESSION_OUTTIME = 10000;
		ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR), SESSION_OUTTIME);
		zkClient.subscribeChildChanges("/serverList", new IZkChildListener() {
			@Override
			public void handleChildChange(String s, List<String> list) throws Exception {
				System.out.println("参数s：" + s);
				System.out.println("参数list：" + list);
			}
		});
		System.out.println("zkClient初始化完成。。。");
		Scanner scanner = new Scanner(System.in);
		scanner.nextInt();

	}
}
