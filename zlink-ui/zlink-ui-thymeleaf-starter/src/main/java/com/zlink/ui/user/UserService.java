package com.zlink.ui.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zlink.ui.user.dto.*;
import com.zlink.ui.user.vo.Role;
import com.zlink.ui.user.vo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserInfoMapper userMapper;
    @Autowired
    private UserRoleMapper roleMapper;
    @Autowired
    private UserPassMapper passMapper;
    @Autowired
    private RoleInfoMapper roleInfoMapper;

    public User getUser(String userId) {
        User user = new User();
        UserInfo userInfo = userMapper.selectById(userId);
        if (userInfo != null) {
            user.setUserId(userInfo.getUserId());
            user.setUserName(userInfo.getUserName());
            user.setUserIcon(userInfo.getUserIcon());
            user.setCellphone(userInfo.getCellphone());
            user.setEmail(userInfo.getEmail());
            user.setSex(userInfo.getSex());
            user.setRemarks(userInfo.getRemarks());
            UserRole role = roleMapper.selectById(userId);
            if (role != null) {
                user.setRoleId(role.getRoleId());
            }
            UserPass pass = passMapper.selectById(userId);
            if (pass != null) {
                user.setPassword(pass.getPassword());
            }
        }
        return user;
    }

    public List<User> getAll() {
        return getAll(null);
    }

    public List<User> getAll(Map<String, String[]> params) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        if (params != null) {
            if (params.containsKey("userId")) {
                String userId = params.get("userId")[0];
                if (!StringUtils.isEmpty(userId)) {
                    queryWrapper.eq("USER_ID", userId);
                }
            }
            if (params.containsKey("userName")) {
                String userName = params.get("userName")[0];
                if (!StringUtils.isEmpty(userName)) {
                    queryWrapper.eq("USER_NAME", userName);
                }
            }
        }
        List<UserInfo> userInfos = userMapper.selectList(queryWrapper);
        List<User> userList = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            User user = new User();
            user.setUserId(userInfo.getUserId());
            user.setUserName(userInfo.getUserName());
            user.setUserIcon(userInfo.getUserIcon());
            user.setCellphone(userInfo.getCellphone());
            user.setEmail(userInfo.getEmail());
            user.setSex(userInfo.getSex());
            user.setRemarks(userInfo.getRemarks());
            userList.add(user);
        }
        return userList;
    }

    public List<Role> getAllRoles() {
        List<RoleInfo> roleInfos = roleInfoMapper.selectList(new QueryWrapper<>());
        List<Role> roleList = new ArrayList<>();
        for (RoleInfo roleInfo : roleInfos) {
            Role role = new Role();
            role.setRoleId(roleInfo.getRoleId());
            role.setRoleName(roleInfo.getRoleName());
            role.setRoleDesc(roleInfo.getRoleDesc());
            roleList.add(role);
        }
        return roleList;
    }

    public void update(User user) {
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("USER_ID", user.getUserId());
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        userMapper.update(userInfo, updateWrapper);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(user.getRoleId());
        roleMapper.update(userRole, updateWrapper);
    }
}
