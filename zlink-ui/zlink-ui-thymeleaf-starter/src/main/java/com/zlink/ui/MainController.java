package com.zlink.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    @RequestMapping({"/", "/index"})
    public String index() {
        return "/index";
    }

    @RequestMapping({"/welcome.html"})
    public String welcome() {
        return "/welcome";
    }

    @RequestMapping({
            "/user/**/*.html",
            "/system/**/*.html",
            "/bar/**/*.html",
            "/set/**/*.html",
            "/app/**/*.html",
            "/examples/**/*.html"
    })
    public String view(HttpServletRequest request) {
        return request.getRequestURI().replaceAll(".html", "");
    }
}
