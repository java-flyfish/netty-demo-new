package com.rpc.config;

import lombok.Data;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class ConfigurationFactory {
    String url;
    public ConfigurationFactory(){
        this.url = "dubbo.xml";
    }
    public ConfigurationFactory(String url){
        this.url = url;
    }

    public <T> T analysisXML(Class<T> tClass){
        T instance = null;
        try {
            instance = tClass.newInstance();
            analysisXML(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public <T> void analysisXML(T instance){
        // 解析books.xml文件
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            InputStream in = this.getClass().getResourceAsStream(url);
            URL resource = this.getClass().getClassLoader().getResource(url);
            Document document = reader.read(new File(resource.getFile()));
            // 通过document对象获取根节点bookstore
            Element element = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator it = element.elementIterator();
            // 遍历迭代器，获取根节点中的信息（书籍）
            Class aClass = instance.getClass();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                // 获取Element的属性名以及 属性值
                List<Attribute> conifgAttrs = e.attributes();
                Method[] methods = aClass.getMethods();
                for (Method m : methods) {
                    if (m.getName().equalsIgnoreCase("set" + conifgAttrs.get(0).getValue())){
                        m.invoke(instance,conifgAttrs.get(1).getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
