package com.zlink.ui.user.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("USER_ROLE")
public class UserRole {
    @TableId
    private String userId;
    private String roleId;
}
