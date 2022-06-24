package com.zlink.ui.menu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Menu {
    private String id;
    private String name;
    private String title;
    private String icon;
    private String href;
    private List<Menu> list = new ArrayList<>();

    public void addChild(Menu menu) {
        list.add(menu);
    }

    public Menu getChild(String childId) {
        for (Menu m : list) {
            if (m.getId().equals(childId)) {
                return m;
            }
        }
        return null;
    }
}
