package external.ogm.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * For reference: https://github.com/neo4j/neo4j-ogm/blob/2.0/core/src/test/java/org/neo4j/ogm/domain/canonical/Mappable.java
 */
@NodeEntity
public class ArrayHolder {

    @GraphId
    public Long id;

    public Integer[] intArr;

    public String[] stringArr;
}
