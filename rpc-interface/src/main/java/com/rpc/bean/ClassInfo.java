package com.rpc.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClassInfo implements Serializable {
    private String className; //类名
    private String methodName; //方法名
    private Class<?>[] types; //参数类型
    private Object[] objects; //参数列表
}
