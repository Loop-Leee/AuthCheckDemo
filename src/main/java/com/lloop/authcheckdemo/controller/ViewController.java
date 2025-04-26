package com.lloop.authcheckdemo.controller;

import com.lloop.authcheckdemo.model.request.UserEditRequest;
import com.lloop.authcheckdemo.model.request.UserRegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/index")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/")
    public String indexPage2() {
        return "index";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login"; // 返回模板路径（不带 .html）
    }

    @GetMapping("/user/homepage")
    public String homepage() {
        return "user/homepage"; // 返回模板路径（不带 .html）
    }

    @GetMapping("/auth/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRegisterRequest", new UserRegisterRequest());
        return "auth/register";
    }

    @GetMapping("/auth/info")
    public String showInfoPage(Model model) {
        model.addAttribute("userEditRequest", new UserEditRequest());
        return "user/info";
    }

}