package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class NodeMenuController {

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Transactional
    @RequestMapping("/nodeMenu")
    public Set<ArgumentNode> getNodeMenu() {

        return argumentNodeRepository.getAllNodesRich();
    }

    @Transactional
    @RequestMapping("/rootNodes")
    public Set<ArgumentNode> getRootNodes() {

        return argumentNodeRepository.getRootNodesRich();
    }

}
