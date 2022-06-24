package com.zlink.ui.user.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ROLE_INFO")
public class RoleInfo {
    @TableId
    private String roleId;
    private String roleName;
    private String roleDesc;
}
