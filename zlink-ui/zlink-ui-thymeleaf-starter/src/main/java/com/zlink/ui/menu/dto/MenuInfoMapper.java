package com.zlink.ui.menu.dto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuInfoMapper extends BaseMapper<MenuInfo> {

    @Select("<script>"
            + "SELECT * FROM MENU_INFO WHERE MENU_ID IN "
            + "<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + " ORDER BY MENU_ID"
            + "</script>")
    List<MenuInfo> selectByIdsWithSort(List<String> list);

}
