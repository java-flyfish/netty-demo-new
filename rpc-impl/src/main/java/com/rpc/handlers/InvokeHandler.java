package com.rpc.handlers;

import com.rpc.bean.ClassInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * created by weiyang
 */
@ChannelHandler.Sharable
@Data
public class InvokeHandler extends ChannelInboundHandlerAdapter{
    private ApplicationContext applicationContext;

    public InvokeHandler (ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //直接转换传过来的数据
        ClassInfo classInfo = (ClassInfo)msg;
//        String className = getImplClassName(classInfo);
        Class clzz = Class.forName(classInfo.getClassName());
        Object bean = applicationContext.getBean(clzz);
        Method method = bean.getClass().getMethod(classInfo.getMethodName(),classInfo.getTypes());
        Object result = method.invoke(bean, classInfo.getObjects());
        //写出数据到通道中
        ctx.writeAndFlush(result);
    }

    /**
     * 获取目标类名
     * @param classInfo
     * @return
     * @throws Exception
     */
    private String getImplClassName(ClassInfo classInfo) throws Exception {
        //服务方接口和实现类所在的包路径
        String interfacePath = "com.rpc";
        int lastDot = classInfo.getClassName().lastIndexOf(".");
        String interfaceName = classInfo.getClassName().substring(lastDot);
        Class superClass = Class.forName(interfacePath + interfaceName);
        Reflections reflections = new Reflections(interfacePath);
        //得到某接口下的所有实现类
        Set<Class> ImplClassSet = reflections.getSubTypesOf(superClass);
        if (ImplClassSet.size() == 0) {
            System.out.println("未找到实现类");
            return null;
        } else if (ImplClassSet.size() > 1) {
            System.out.println("找到多个实现类， 未明确使用哪一个");
            return null;
        } else {
            //把集合转换为数组
            Class[] classes = ImplClassSet.toArray(new Class[0]);
            return classes[0].getName(); //得到实现类的名字
        }
    }


}
