package com.nodestand.dao;

import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NodeListDao {

    public static List<Object> getAllNodes(GraphDatabase graphDatabase) {

            Result<Map<String, Object>> result = graphDatabase.queryEngine().query(
                    "match (argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(author:User) " +
                            "return { id: id(argument), title: body.title, labels: labels(argument), " +
                            "author: author.displayName, " +
                            "version: body.majorVersion + '.' + body.minorVersion + '.' + argument.buildVersion} as Obj", null);

            List<Object> nodes = new LinkedList<>();

            for (Map<String, Object> map: result) {
                nodes.add(map.get("Obj"));
            }

            return nodes;
    }

}
