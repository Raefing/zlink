package com.zlink.ui.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Password {
    private String userId;
    private String oldPassword;
    private String newPassword;
}
