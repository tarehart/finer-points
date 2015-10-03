package com.nodestand.dao;

import org.neo4j.ogm.session.result.Result;
import org.springframework.data.neo4j.template.Neo4jTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NodeListDao {

    public static List<Object> getAllNodes(Neo4jTemplate neo4jTemplate) {

            Result result = neo4jTemplate.query(
                    "match (argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(author:User), " +
                            "(body)-[VERSION_OF]->(mv:MajorVersion) " +
                            "return { id: id(argument), title: body.title, labels: labels(argument), " +
                            "author: author.displayName, " +
                            "version: mv.versionNumber + '.' + body.minorVersion + '.' + argument.buildVersion} as Obj", null);

            List<Object> nodes = new LinkedList<>();

            for (Map<String, Object> map: result) {
                nodes.add(map.get("Obj"));
            }

            return nodes;
    }

}
