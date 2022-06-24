package com.zlink.protocol.base.data;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@Accessors(chain = true, fluent = true)
public class HttpData {

    private String path;
    private String query;
    private String method;
    private byte[] data;
    private int statusCode;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
}
