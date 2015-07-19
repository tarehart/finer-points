package com.nodestand.nodes.version;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VersionHelper {

    private static final String CURRENT_MAX_KEY = "currentMax";

    @Autowired
    GraphDatabase graphDatabase;

    @Autowired
    ArgumentNodeRepository nodeRepository;

    /**
     * This sets the major and minor version on the draft body.
     * @param body
     * @return
     */
    public static void decorateDraftBody(ArgumentBody body) {

        if (body.getMajorVersion() == null) {
            // If the major version is null, we assume this is a brand new body, i.e. version 1.0.0.
            // Normally we would wait until publish to set the version numbers, but we already know what they are.
            // Doing it eagerly because why not.
            MajorVersion mv = new MajorVersion(1, new VersionAggregator());
            body.setVersion(mv, 0);
        } else {
            // A minor version of -1 is set for all drafts. It will receive a real version number later
            // when it is published.
            body.setMinorVersion(-1);
        }


    }

    public Build startBuild(ArgumentBody body) {
        Build build = new Build();
        build.author = body.author;
        return build;
    }

    private int getNextMinorVersion(MajorVersion majorVersion) {
        Map<String, Object> params = new HashMap<>();
        params.put( "id", majorVersion.id );

        Result<Map<String, Object>> result = graphDatabase.queryEngine().query("start n=node({id}) " +
                "match body-[VERSION_OF]->n " +
                "return max(body.minorVersion) as " + CURRENT_MAX_KEY, params);

        Map<String, Object> resultMap = result.singleOrNull();
        if (resultMap != null) {
            int currentMax = (int) resultMap.get(CURRENT_MAX_KEY);
            return currentMax + 1;
        }

        return 0;
    }

    private Node getLockNode(MajorVersion mv) {
        return graphDatabase.getNodeById(mv.getId());
    }

    public void publish(ArgumentNode node) {

        // TODO: validate that the node and its descendants follow all the rules, e.g. being grounded in sources

        try ( Transaction tx = graphDatabase.beginTx() ) {
            ArgumentBody body = node.getBody();
            if (body.getMinorVersion() < 0) {
                tx.acquireWriteLock(getLockNode(body.getMajorVersion()));
                body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
            }
            node.setVersion(0);
            nodeRepository.save(node);
            tx.success();
        }
        
        // TODO: make new nodes with new version numbers for all consumers. Decorate with the Build object.

        // TODO: worry about concurrency for setting build numbers.
    }
}
