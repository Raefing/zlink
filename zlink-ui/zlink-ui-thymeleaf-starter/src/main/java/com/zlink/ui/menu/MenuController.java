package com.zlink.ui.menu;

import com.zlink.ui.ResponseData;
import com.zlink.ui.menu.dto.MenuInfo;
import com.zlink.ui.menu.vo.Menu;
import com.zlink.ui.user.UserService;
import com.zlink.ui.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;

    @GetMapping("/menus")
    public ResponseData menuAll() {
        List<MenuInfo> menuList = menuService.getMenuAll();
        return ResponseData.success().setData(menuList);
    }

    @GetMapping("/menu")
    public ResponseData menu(@RequestParam("userId") String userId) {
        log.error("--->userId:{}", userId);
        User user = userService.getUser(userId);
        List<Menu> menuList = menuService.getMenuByRoleId(user.getRoleId());
        return ResponseData.success().setData(menuList);
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseData deleteMenu(@PathVariable("menuId") String menuId) {
        menuService.deleteMenu(menuId);
        return ResponseData.success();
    }

    @PostMapping("/menu")
    public ResponseData addMenu(@RequestBody MenuInfo menu) {
        menuService.addMenu(menu);
        return ResponseData.success();
    }

    @PutMapping("/menu")
    public ResponseData updateMenu(@RequestBody MenuInfo menu) {
        menuService.updateMenu(menu);
        return ResponseData.success();
    }
}
