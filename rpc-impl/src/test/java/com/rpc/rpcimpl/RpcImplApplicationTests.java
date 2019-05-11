package com.rpc.rpcimpl;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RpcImplApplicationTests {

	@Autowired
	ApplicationContext applicationContext;
	@Test
	public void contextLoads() throws ClassNotFoundException {

		Class clzz = Class.forName("com.rpc.service.HelloNetty");
		Object bean = applicationContext.getBean(clzz);

		System.out.println(bean);
	}

	@Test
	public void testZkClient() throws InterruptedException {
		/** zookeeper地址 */
//		String CONNECT_ADDR = "192.168.1.31:2181,192.168.1.32:2181,192.168.1.33:2181";
		String CONNECT_ADDR = "10.0.4.165:2181";
		/** session超时时间 */
		int SESSION_OUTTIME = 10000;//ms

		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), SESSION_OUTTIME);
		//1. create and delete方法

		zkc.createEphemeral("/temp");
		zkc.createPersistent("/super/c1", true);
		Thread.sleep(10000);
		zkc.delete("/temp");
		zkc.deleteRecursive("/super");

		//2. 设置path和data 并且读取子节点和每个节点的内容
		zkc.createPersistent("/super", "1234");
		zkc.createPersistent("/super/c1", "c1内容");
		zkc.createPersistent("/super/c2", "c2内容");
		List<String> list = zkc.getChildren("/super");
		for(String p : list){
			System.out.println(p);
			String rp = "/super/" + p;
			String data = zkc.readData(rp);
			System.out.println("节点为：" + rp + "，内容为: " + data);
		}

		//3. 更新和判断节点是否存在
		zkc.writeData("/super/c1", "新内容");
		System.out.println(zkc.readData("/super/c1").toString());
		System.out.println(zkc.exists("/super/c1"));

//		4.递归删除/super内容
		zkc.deleteRecursive("/super");
	}

	@Test
	public void testAddress() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		System.out.println(address.getHostAddress());
	}
}
