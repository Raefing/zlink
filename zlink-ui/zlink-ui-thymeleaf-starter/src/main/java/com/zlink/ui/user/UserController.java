package com.zlink.ui.user;

import com.zlink.ui.ResponseData;
import com.zlink.ui.img.ImageLoader;
import com.zlink.ui.user.vo.Password;
import com.zlink.ui.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ImageLoader imageLoader;

    @GetMapping("/user/login")
    public ResponseData login(HttpSession session, @RequestParam("userId") String userId, @RequestParam("password") String password) {
        User user = userService.getUser(userId);
        if (user != null) {
            if (!password.equals(user.getPassword())) {
                return ResponseData.error("用户名/密码错误");
            } else {
                //隔离密码
                user.setPassword("");
                session.setAttribute("user", user);
                return ResponseData.success().setData(user);
            }
        } else {
            return ResponseData.error("用户名/密码错误");
        }
    }

    @PostMapping("/user/edit")
    public ResponseData edit(@RequestBody User user) {
        log.error("{}", user);
        user.setUserName("你好1111");
        return ResponseData.success().setData(user);
    }

    @PostMapping("/user/password")
    public ResponseData pass(@RequestBody Password password) {
        return ResponseData.success();
    }

    @GetMapping("/user/list")
    public ResponseData list(HttpServletRequest request) {
        Map<String, String[]> queryMap = request.getParameterMap();
        List<User> userList = userService.getAll(queryMap);
        if (userList != null && !userList.isEmpty()) {
            return ResponseData.success().setData(userList);
        } else {
            return ResponseData.error("没有查询到对应数据");
        }
    }

    @GetMapping("/user/img/{id}")
    public void img(HttpServletResponse response, @PathVariable("id") String id) {
        User user = userService.getUser(id);
        OutputStream os = null;
        try {
            response.setContentType("image/jpg");
            os = response.getOutputStream();
            os.write(imageLoader.getImgData(user.getUserIcon()));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/user/allRoles")
    public ResponseData allRoles(){
        return ResponseData.success().setData(userService.getAllRoles());
    }
}
