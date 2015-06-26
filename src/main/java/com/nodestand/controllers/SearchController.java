package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.repository.AbstractGraphRepository;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.NodeGraphRepositoryImpl;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@RestController
public class SearchController {

    ArgumentBodyRepository repo;

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    public SearchController(Neo4jTemplate template) {
        repo = new ArgumentBodyRepository(template);
    }

    // TODO: uncomment the authorization
    //@PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @RequestMapping("/search")
    public List<ArgumentBody> findByTitle(@RequestParam String query) {

        // Second object is either a Lucene query object or a query string.
        Result<ArgumentBody> result = repo.findAllByQuery("body-search", "title", query + "*");

        List<ArgumentBody> searchResults = new LinkedList<>();

        HashSet<Long> majorVersionIds = new HashSet<>();

        // Keep the search results in order. If there are multiple bodies corresponding to
        // the same major version node, return only the first one.

        Iterator<ArgumentBody> iterator = result.iterator();
        while (iterator.hasNext()) {
            ArgumentBody body = iterator.next();
            long majorVersionId = body.getMajorVersion().getId(); // This is NOT the version number; it's the unique node id
            if (!majorVersionIds.contains(majorVersionId)) {
                searchResults.add(body);
                majorVersionIds.add(majorVersionId);
            }
        }

        return searchResults;
    }
}
