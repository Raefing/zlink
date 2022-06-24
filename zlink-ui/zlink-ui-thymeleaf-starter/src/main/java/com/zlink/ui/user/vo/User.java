package com.zlink.ui.user.vo;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String userId;
    private String userName;
    private String password;
    private String roleId;
    private String sex;
    private String userIcon;
    private String cellphone;
    private String email;
    private String remarks;
}
