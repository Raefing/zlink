package com.zlink.ui.user.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("USER_INFO")
public class UserInfo {
    @TableId
    private String userId;
    private String userName;
    private String sex;
    private String cellphone;
    private String email;
    private String remarks;
    private String userIcon;
}
