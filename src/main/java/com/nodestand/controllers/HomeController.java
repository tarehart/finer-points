package com.nodestand.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String getIndex() {

        return "index";
    }

}
