package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@RestController
public class SearchController {

    ArgumentBodyRepository repo;

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

//    @Autowired
//    public SearchController(Neo4jTemplate template) {
//        repo = new ArgumentBodyRepository(template);
//    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @RequestMapping("/search")
    public List<ArgumentBody> findByTitle(@RequestParam String query, @RequestParam List<String> types) {

        User user = nodeUserDetailsService.getUserFromSession();

        // Second object is either a Lucene query object or a query string.
        //Result<ArgumentBody> result = repo.findAllByQuery("title-search", "title", query + "*");

        List<ArgumentBody> searchResults = new LinkedList<>();

        HashSet<Long> majorVersionIds = new HashSet<>();

        List<Class> acceptableClasses = buildAcceptableClasses(types);

        // Keep the search results in order. If there are multiple bodies corresponding to
        // the same major version node, return only the first one.
//        Iterator<ArgumentBody> iterator = result.iterator();
//        while (iterator.hasNext()) {
//            ArgumentBody body = iterator.next();
//            if (acceptableClasses.contains(body.getClass())) {
//                long majorVersionId = body.getMajorVersion().getId(); // This is NOT the version number; it's the unique node id
//                if (!majorVersionIds.contains(majorVersionId) && (!body.isDraft() || user.getNodeId() == body.author.getNodeId())) {
//                    searchResults.add(body);
//                    majorVersionIds.add(majorVersionId);
//                }
//            }
//        }

        return searchResults;
    }

    private List<Class> buildAcceptableClasses(List<String> types) {
        List<Class> acceptableClasses = new ArrayList<>();
        if (types.contains("assertion")) {
            acceptableClasses.add(AssertionBody.class);
        }
        if (types.contains("interpretation")) {
            acceptableClasses.add(InterpretationBody.class);
        }
        if (types.contains("source")) {
            acceptableClasses.add(SourceBody.class);
        }
        return acceptableClasses;
    }
}
