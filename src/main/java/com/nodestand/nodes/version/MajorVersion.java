package com.nodestand.nodes.version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.*;

import java.util.*;

@NodeEntity
public class MajorVersion implements Commentable {

    @GraphId
    protected Long id;

    private String stableId;

    private int versionNumber;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    protected VersionAggregator versionAggregator;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public Author author;

    // I'm not using a map for this because you're not allowed to save maps on nodes.
    public int greatVotes;
    public int weakVotes;
    public int toucheVotes;
    public int trashVotes;

    @Property
    private String[] edgeOwners;

    public MajorVersion() {}

    public MajorVersion(int versionNumber, VersionAggregator versionAggregator, Author author) {
        this.versionNumber = versionNumber;
        this.versionAggregator = versionAggregator;
        this.stableId = IdGenerator.newId();
        this.author = author;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    @JsonIgnore
    @Transient
    public Set<User> getCommentWatchers() {
        // TODO: Allow people to watch / unwatch major versions.
        return Collections.emptySet();
    }

    public String getStableId() {
        return stableId;
    }

    public void decrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes--;
                break;
            case WEAK:
                weakVotes--;
                break;
            case TOUCHE:
                toucheVotes--;
                break;
            case TRASH:
                trashVotes--;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }

    public void incrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes++;
                break;
            case WEAK:
                weakVotes++;
                break;
            case TOUCHE:
                toucheVotes++;
                break;
            case TRASH:
                trashVotes++;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }

    /**
     * This is a map of majorVersionStableId -> authorStableId.
     *
     * Mutating the returned map and then saving the node will have no effect.
     * You need to use the setter.
     */
    public Map<String, String> getEdgeOwners() {

        Map<String, String> owners = new HashMap<>();
        if (edgeOwners == null) {
            return owners; // TODO: some kind of backfill?
        }

        for (String s: edgeOwners) {
            String[] entity = s.split(" ");
            owners.put(entity[0], entity[1]);
        }
        return owners;
    }

    private void setEdgeOwners(Map<String, String> owners) {
        edgeOwners = new String[owners.size()];

        int i = 0;
        for (Map.Entry<String, String> entry: owners.entrySet()) {
            edgeOwners[i++] = String.format("%s %s", entry.getKey(), entry.getValue());
        }
    }

    public void mergeEdgeOwner(Author author, String majorVersionStableId) {
        Set<String> single = new HashSet<>();
        single.add(majorVersionStableId);
        mergeEdgeOwners(author, single);
    }

    public void mergeEdgeOwners(Author author, Collection<String> majorVersionStableId) {
        Map<String, String> edgeOwners = getEdgeOwners();
        for (String mvId: majorVersionStableId) {
            if (!edgeOwners.containsKey(mvId)) {
                edgeOwners.put(mvId, author.getStableId());
            }
        }
        setEdgeOwners(edgeOwners);
    }
}
