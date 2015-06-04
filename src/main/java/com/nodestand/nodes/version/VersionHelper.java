package com.nodestand.nodes.version;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
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

    public Build beginBodyBuild(ArgumentBody body) {
        Build build = new Build();
        build.author = body.author;

        VersionAggregator aggregator = new VersionAggregator();

        if (body.getMajorVersion() == null) {
            MajorVersion mv = new MajorVersion(1, aggregator);
            body.setVersion(mv, 0);
        } else {
            body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
        }

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


}
