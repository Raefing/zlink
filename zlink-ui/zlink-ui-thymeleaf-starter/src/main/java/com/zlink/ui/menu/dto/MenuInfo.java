package com.zlink.ui.menu.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("MENU_INFO")
public class MenuInfo {
    @TableId
    private String menuId;
    private String menuName;
    private String menuTitle;
    private String menuIcon;
    private String menuHref;
    private String superMenuId;
    private String menuDesc;
}
