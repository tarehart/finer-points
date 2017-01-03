package com.nodestand.service.vote;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.VoteType;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Tyler on 1/1/2017.
 */
@Component
public class VoteServiceNeo4j implements VoteService {

    private static final int MAX_EDGE_MULTIPLIER = 3;
    private static final int INTERPRETATION_MULTIPLIER = 2;
    private static final int SOURCE_MULTIPLIER = 2;

    private final ArgumentNodeRepository argumentNodeRepository;
    private final UserRepository userRepository;
    private final Session session;

    @Autowired
    public VoteServiceNeo4j(ArgumentNodeRepository argumentNodeRepository, UserRepository userRepository, Session session) {
        this.argumentNodeRepository = argumentNodeRepository;
        this.userRepository = userRepository;
        this.session = session;
    }

    @Override
    public void voteNode(String userStableId, String nodeStableId, VoteType voteType) throws NodeRulesException {

        User user = userRepository.loadUserWithVotes(userStableId);
        ArgumentNode node = argumentNodeRepository.getNodeRich(nodeStableId);

        Author originalAuthor = node.getBody().getMajorVersion().author;
        if (isOwnAuthor(originalAuthor, user)) {
            throw new NodeRulesException("Can't vote on your own node.");
        }

        MajorVersion mv = node.getBody().getMajorVersion();

        Optional<ArgumentVote> existingVote = user.getExistingVote(mv);

        if (existingVote.isPresent()) {
            ArgumentVote vote = existingVote.get();
            VoteType oldVote = vote.voteType;
            if (vote.voteType.equals(voteType)) {
                return; // Nothing to do.
            }
            vote.voteType = voteType; // This mutates the user's vote

            mv.decrementVote(oldVote);
            mv.incrementVote(voteType);

            updateScore(node, voteType, oldVote, user);

        } else {
            ArgumentVote newVote = new ArgumentVote();
            newVote.voteType = voteType;
            newVote.majorVersion = mv;
            newVote.user = user;

            user.registerNewVote(newVote);

            mv.incrementVote(voteType);

            updateScore(node, voteType, null, user);
        }

        session.save(mv);
        session.save(user);
    }

    @Override
    public void unvoteNode(String nodeStableId, String userStableId) throws NodeRulesException {

        // Scenario: Card created by A, card voted green, card edited with additional edge by B,
        // card vote revoked. A will net 0 points, and B will net negative points. This is acceptable
        // because it seems like B made things worse.

        User user = userRepository.loadUserWithVotes(userStableId);
        ArgumentNode node = argumentNodeRepository.getNodeRich(nodeStableId);

        MajorVersion mv = node.getBody().getMajorVersion();

        Optional<ArgumentVote> existingVote = user.getExistingVote(mv);

        if (existingVote.isPresent()) {
            ArgumentVote vote = existingVote.get();

            user.revokeVote(mv.getStableId());

            mv.decrementVote(vote.voteType);

            updateScore(node, null, vote.voteType, user);

            session.save(mv);
            session.save(user);
        }
    }

    private boolean isOwnAuthor(Author author, User user) {
        // Assumes that author.getUser will be non-null if it's the logged in user,
        // because that user should already be loaded in the session context and neo4j-ogm links
        // things nicely like that.
        return author.getUser() != null && author.getUser().getStableId().equals(user.getStableId());
    }

    private void updateScore(ArgumentNode node, VoteType voteType, VoteType voteTypeToNegate, User voter) {
        if ("source".equals(node.getType())) {
            Author author = node.getBody().getMajorVersion().author;
            if (!isOwnAuthor(author, voter)) { // Can't alter your own points.
                updatePointsAndSave(node, voteType, voteTypeToNegate, author, 0);
            }
        } else {
            MajorVersion mv = node.getBody().getMajorVersion();
            Set<String> childrenMajorVersions = node.getBody().getMajorVersionsFromBodyText();
            Map<String, String> edgeOwners = mv.getEdgeOwners();


            Map<String, Long> authorIdCounts = childrenMajorVersions.stream()
                    .map(mvId -> edgeOwners.containsKey(mvId) ? edgeOwners.get(mvId) : "")
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            // Now fetch the owners from the DB and give them points.
            for (Map.Entry<String, Long> entry: authorIdCounts.entrySet()) {
                if (!entry.getKey().isEmpty()) { // Might be empty if the mapping on the MV was incomplete
                    Author author = userRepository.loadAuthor(entry.getKey());
                    if (!isOwnAuthor(author, voter)) { // Can't alter your own points.
                        updatePointsAndSave(node, voteType, voteTypeToNegate, author, Math.toIntExact(entry.getValue()));
                    }
                }
            }

        }
    }

    private void updatePointsAndSave(ArgumentNode node, VoteType voteType, VoteType voteTypeToNegate, Author author, int edgesOwned) {
        int points = 0;

        if (voteType != null) {
            points += getPoints(voteType, node.getType(), edgesOwned);
        }

        if (voteTypeToNegate != null) {
            points += getPoints(voteTypeToNegate, node.getType(), edgesOwned) * -1;
        }

        author.awardNodePoints(points);

        // TODO: log the award

        session.save(author);
    }


    private int getPoints(VoteType voteType, String nodeType, int numEdgesOwned) {
        int baseValue = getBaseValue(voteType);

        switch (nodeType) {
            case "assertion":
                return baseValue * Math.min(numEdgesOwned, MAX_EDGE_MULTIPLIER);
            case "interpretation":
                return baseValue * INTERPRETATION_MULTIPLIER;
            case "source":
                return baseValue * SOURCE_MULTIPLIER;
            default: return 0;
        }
    }

    private int getBaseValue(VoteType voteType) {
        switch (voteType) {
            case GREAT: return 10;
            case WEAK: return 2;
            case TOUCHE: return 6;
            case TRASH: return -4;
            default: return 0;
        }
    }
}
