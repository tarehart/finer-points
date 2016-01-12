package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    Session session;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/currentUser")
    public User currentUser() throws NodeRulesException {

        Long userId = nodeUserDetailsService.getUserIdFromSession();
        if (userId == null) {
            return null;
        }
        return session.load(User.class, userId);
    }
}
