package com.nodestand.controllers;

import com.nodestand.nodes.User;
import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @RequestMapping("/")
    public String getIndex(Model model) {

        User user = nodeUserDetailsService.getUserFromSession();
        model.addAttribute("user", user);

        return "index";
    }

}
