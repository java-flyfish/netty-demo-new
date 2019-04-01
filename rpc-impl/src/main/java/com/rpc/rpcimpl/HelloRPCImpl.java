package com.rpc.rpcimpl;

import com.rpc.service.HelloRPC;

public class HelloRPCImpl implements HelloRPC {
    @Override
    public String hello(String name) {
        return "hello" + name;
    }
}
