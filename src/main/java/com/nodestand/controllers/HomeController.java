package com.nodestand.controllers;

import com.nodestand.nodes.User;
import com.nodestand.service.user.UserService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    Session session;

    @RequestMapping("/")
    public String getIndex(Model model) {

        Long userId = userService.getUserIdFromSession();
        if (userId != null) {
            User user = session.load(User.class, userId);
            model.addAttribute("user", user);
        }

        return "index";
    }

}
