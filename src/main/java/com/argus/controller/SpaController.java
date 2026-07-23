package com.argus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/signin",
        "/signup",
        "/verify-email",
        "/demo/**",
        "/dashboard/**",
        "/incident/**",
        "/analytics/**",
        "/settings/**",
        "/diagnostics/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
