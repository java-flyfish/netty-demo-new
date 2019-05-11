package com.rpc;

import com.rpc.config.ConfigurationFactory;
import com.rpc.config.DubboConfiguration;
import com.rpc.proxy.NettyRPCProxy;
import com.rpc.service.HelloNetty;
import com.rpc.service.HelloRPC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RpcClientApplicationTests {

	@Test
	public void contextLoads() {
		NettyRPCProxy<HelloNetty> proxy = new NettyRPCProxy();
		HelloNetty helloNetty= proxy.getBean(HelloNetty.class);
		System.out.println(helloNetty.hello());
		//第 2 次远程调用
		HelloRPC helloRPC = (HelloRPC) proxy.create(HelloRPC.class);
		System.out.println(helloRPC.hello("RPC"));
	}

	@Test
	public void testAnalysisXml(){
		ConfigurationFactory factory = new ConfigurationFactory();
		DubboConfiguration configuration = factory.analysisXML(DubboConfiguration.class);
		System.out.println(configuration);
	}
}
