package com.zlink.ui.menu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zlink.ui.menu.dto.MenuInfo;
import com.zlink.ui.menu.dto.MenuInfoMapper;
import com.zlink.ui.menu.dto.RoleMenu;
import com.zlink.ui.menu.dto.RoleMenuMapper;
import com.zlink.ui.menu.vo.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    @Autowired
    private MenuInfoMapper menuInfoMapper;
    @Autowired
    private RoleMenuMapper roleMenuMapper;

    public List<MenuInfo> getMenuAll(){
        return menuInfoMapper.selectList(new QueryWrapper<>());
    }

    public void deleteMenu(String menuId){
        menuInfoMapper.deleteById(menuId);
    }

    public List<Menu> getMenuByRoleId(String roleId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ROLE_ID", roleId);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        if (roleMenus != null) {
            List<String> ids = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
            List<MenuInfo> list = menuInfoMapper.selectByIdsWithSort(ids);
            List<MenuInfo> cp = new ArrayList<>();
            List<Menu> roots = new ArrayList<>();
            for (MenuInfo menuInfo : list) {
                if (StringUtils.isEmpty(menuInfo.getSuperMenuId())) {
                    Menu menu = new Menu();
                    menu.setId(menuInfo.getMenuId());
                    menu.setName(menuInfo.getMenuName());
                    menu.setTitle(menuInfo.getMenuTitle());
                    menu.setIcon(MenuIconMapper.map(menuInfo.getMenuIcon()));
                    menu.setHref(menuInfo.getMenuHref());
                    roots.add(menu);
                } else {
                    cp.add(menuInfo);
                }
            }
            while (cp.size() > 0) {
                MenuInfo menuInfo = cp.remove(0);
                for (Menu root : roots) {
                    if (root.getId().equals(menuInfo.getSuperMenuId())) {
                        addChild(root, menuInfo);
                        break;
                    } else {
                        Menu target = getChild(root, menuInfo.getSuperMenuId());
                        if (target != null) {
                            addChild(target, menuInfo);
                            break;
                        }
                    }
                }
            }
            return roots;
        }
        throw new RuntimeException("角色[" + roleId + "]为找到对应的菜单授权");
    }

    private Menu getChild(Menu root, String id) {
        Menu menu = root.getChild(id);
        if (menu == null) {
            List<Menu> list = root.getList();
            if (list != null && list.size() > 0) {
                for (Menu u : list) {
                    Menu r = getChild(u, id);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        return menu;
    }

    private void addChild(Menu root, MenuInfo child) {
        Menu menu = new Menu();
        menu.setId(child.getMenuId());
        menu.setName(child.getMenuName());
        menu.setTitle(child.getMenuTitle());
        menu.setIcon(MenuIconMapper.map(child.getMenuIcon()));
        menu.setHref(child.getMenuHref());
        root.addChild(menu);
    }

    public void addMenu(MenuInfo menu) {
        menuInfoMapper.insert(menu);
    }

    public void updateMenu(MenuInfo menu) {
        menuInfoMapper.updateById(menu);
    }
}
