package com.zlink.ui.menu.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ROLE_MENU")
public class RoleMenu {
    private String roleId;
    private String menuId;
}
