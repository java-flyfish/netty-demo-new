package com.rpc.rpcimpl;

import com.rpc.service.HelloNetty;
import org.springframework.stereotype.Component;

@Component
public class HelloNettyImpl implements HelloNetty {
    @Override
    public String hello() {
        return "hello,netty";
    }
}
