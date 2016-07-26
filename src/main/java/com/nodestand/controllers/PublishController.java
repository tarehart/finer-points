package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublishController {

    @Autowired
    UserService userService;

    @Autowired
    ArgumentService argumentService;

    /*

    Node:
        isFinalized - Important if the only desired change is versions of links. Bit weird though if an editable node
            and a non-editable node both point to the same body and the body is not opinionated. A non-editable node
            and an editable body is not a good state.
        isPublic - Not sure

    Body:
        isFinalized - Seems natural. If we go with this, it's important to make copies of node AND body upon snapshotting.
        isPublic - Handy for filtering searches


     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/publishNode")
    public QuickGraphResponse publishNode(@RequestBody Map<String, Object> params) throws Exception {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));

        return argumentService.publishNode(userId, nodeId);
    }
}
