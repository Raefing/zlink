package com.zlink.mark.base;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class RegMessage extends AbstractMessage{

    private String serviceGroup;
    private String serviceName;
    //instanceId
    private String serviceId;
    //http://ip:part/pattan ,tcp://ip:port
    private String uri;
    private Map<String,String> metadata;
}
