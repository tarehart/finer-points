package com.nodestand.controllers;

import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    private final UserService userService;

    private final Neo4jOperations operations;

    @Autowired
    public HomeController(UserService userService, Neo4jOperations operations) {
        this.userService = userService;
        this.operations = operations;
    }

    @RequestMapping("/")
    public String getIndex(HttpServletRequest request, Model model) {

        model.addAttribute("user", userService.getUserFromSecurityContext());
        return "index";
    }

}
