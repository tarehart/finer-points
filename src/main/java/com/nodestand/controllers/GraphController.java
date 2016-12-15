package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphController {

    private final ArgumentService argumentService;
    private final UserService userService;

    @Autowired
    public GraphController(ArgumentService argumentService, UserService userService) {
        this.argumentService = argumentService;
        this.userService = userService;
    }

    @RequestMapping("/graph")
    public QuickGraphResponse getGraph(@RequestParam(value="rootStableId", required=true) String rootStableId) {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        return argumentService.getGraph(rootStableId, userId);
    }

    @RequestMapping("/fullDetail")
    public ArgumentNode getFullDetail(@RequestParam(value="stableId", required=true) String stableId) {
        return argumentService.getFullDetail(stableId);
    }

}
