package com.zlink.ui;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResponseData {
    private int code;
    private String msg;
    private String count;
    private Object data;

    private ResponseData() {
    }

    public static ResponseData success() {
        ResponseData data = new ResponseData();
        data.setCode(0);
        return data;
    }

    public static ResponseData error(String msg) {
        return error(1, msg);
    }

    public static ResponseData error(int code, String msg) {
        ResponseData data = new ResponseData();
        data.setCode(code);
        data.setMsg(msg);
        return data;
    }
}
