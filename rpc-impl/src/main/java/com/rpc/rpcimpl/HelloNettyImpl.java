package com.rpc.rpcimpl;

import com.rpc.service.HelloNetty;

public class HelloNettyImpl implements HelloNetty {
    @Override
    public String hello() {
        return "hello,netty";
    }
}
