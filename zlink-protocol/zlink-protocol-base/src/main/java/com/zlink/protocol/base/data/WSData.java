package com.zlink.protocol.base.data;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true, fluent = true)
public class WSData {

    private String url;
    private String nameSpace;
    private String method;
    private Object[] params;
    private Class returnClass;
    private Object[] returnData;

}
