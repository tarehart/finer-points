package com.nodestand.service.vote;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ScoreLogReader {

    private final AWSLogs awsLogs;
    private final ObjectMapper objectMapper;

    @Autowired
    public ScoreLogReader(AWSLogs awsLogs) {
        this.awsLogs = awsLogs;
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
            FilterLogEventsResult filterLogEventsResult = awsLogs.filterLogEvents(request);
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
