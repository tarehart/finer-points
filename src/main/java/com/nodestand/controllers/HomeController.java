package com.nodestand.controllers;

import com.nodestand.nodes.User;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    Session session;

    @RequestMapping("/")
    public String getIndex(Model model) {

        Long userId = nodeUserDetailsService.getUserIdFromSession();
        if (userId != null) {
            User user = session.load(User.class, userId);
            model.addAttribute("user", user);
        }

        return "index";
    }

}
