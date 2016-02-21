package external.ogm.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

    @NodeEntity
    public class Voter {
        @GraphId
        public Long id;
    }
