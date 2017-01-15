package com.nodestand.service.vote;

import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ScoreLogReader {

    private AWSLogsClient awsLogsClient;
    private ObjectMapper objectMapper;

    @Autowired
    public ScoreLogReader(Environment environment) {

        // In some cases it's OK if these are null.
        // I may decide not to provide them and let the AWS SDK fall back
        // to the EC2 role (if we're currently running on EC2).
        // If you're running a dev environment you'll need these though.
        String accessKeyId = environment.getProperty("aws.accessKeyId");
        String secretKey = environment.getProperty("aws.secretKey");
        if (accessKeyId != null && secretKey != null) {
            System.setProperty("aws.accessKeyId", accessKeyId);
            System.setProperty("aws.secretKey", secretKey);
        }

        awsLogsClient = new AWSLogsClient();
        objectMapper = new ObjectMapper();
    }

    public List<ScoreLog> getScoreLogForUser(String userStableId) {

        FilterLogEventsRequest request = new FilterLogEventsRequest();
        request.setInterleaved(true);
        String logGroupName = "finerpoints-env-userScore";
        request.setLogGroupName(logGroupName);
        request.setFilterPattern(String.format("[,,user=%s,...]", userStableId));
        request.setStartTime(DateTime.now().minusWeeks(1).getMillis());

        List<ScoreLog> scoreLogs = new LinkedList<>();

        String token = null;
        do {
            request.setNextToken(token);
            FilterLogEventsResult filterLogEventsResult = awsLogsClient.filterLogEvents(request);
            scoreLogs.addAll(getLogs(filterLogEventsResult));
            token = filterLogEventsResult.getNextToken();
        } while (token != null);

        return scoreLogs;
    }

    private List<ScoreLog> getLogs(FilterLogEventsResult filterLogEventsResult) {

        return filterLogEventsResult.getEvents().stream()
                .map(this::toScoreLog)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private TimestampedScoreLog toScoreLog(FilteredLogEvent ev) {
        String json = ev.getMessage().split(" ", 4)[3];
        try {
            TimestampedScoreLog scoreLog = objectMapper.readValue(json, TimestampedScoreLog.class);
            scoreLog.granterId = null; // Censor this because it's confidential.
            scoreLog.timestamp = ev.getTimestamp();
            return scoreLog;
        } catch (IOException e) {
            return null;
        }
    }

}
