package com.zlink.ui.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        Object user = request.getSession().getAttribute("user");//取出session
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/user/login.html");
            return false;//拦截
        } else {
            return true;//放行
        }
    }
}
