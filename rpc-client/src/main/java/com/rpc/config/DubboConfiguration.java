package com.rpc.config;

import lombok.Data;
import java.io.Serializable;

@Data
public class DubboConfiguration implements Serializable {
    String address;
    String port;
}
