package com.zlink.ui.user.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("USER_PASS")
public class UserPass {
    @TableId
    private String userId;
    private String password;
    private Date updateTime;
}
