package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.argument.ArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphController {

    @Autowired
    ArgumentService argumentService;

    @Autowired
    ArgumentNodeRepository repo;

    @RequestMapping("/graph")
    public QuickGraphResponse getGraph(@RequestParam(value="rootStableId", required=true) String rootStableId) {

        return argumentService.getGraph(rootStableId);
    }

    @RequestMapping("/fullDetail")
    public ArgumentNode getFullDetail(@RequestParam(value="stableId", required=true) String stableId) {
        return argumentService.getFullDetail(stableId);
    }

}
