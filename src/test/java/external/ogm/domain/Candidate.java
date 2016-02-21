package external.ogm.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

    public class Candidate {

        @GraphId
        public Long id;

        @Relationship(type="VOTE", direction = Relationship.INCOMING)
        public Set<Voter> voters;
    }
