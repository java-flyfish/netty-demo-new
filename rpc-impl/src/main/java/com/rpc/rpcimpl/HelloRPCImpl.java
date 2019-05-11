package com.rpc.rpcimpl;

import com.rpc.service.HelloRPC;
import org.springframework.stereotype.Component;

@Component
public class HelloRPCImpl implements HelloRPC {
    @Override
    public String hello(String name) {
        return "hello" + name;
    }
}
