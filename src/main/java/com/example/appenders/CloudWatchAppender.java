package com.example.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

import java.util.LinkedList;
import java.util.Queue;

public class CloudWatchAppender extends AppenderBase<ILoggingEvent> {
    private CloudWatchLogsClient client;
    private String logGroupName;
    private String logStreamPrefix;

    private Queue<InputLogEvent> eventQueue;

    public void setLogGroupName(String logGroupName) {
        this.logGroupName = logGroupName;
    }

    public void setLogStreamPrefix(String logStreamPrefix) {
        this.logStreamPrefix = logStreamPrefix;
    }

    public String getLogGroupName() {
        return logGroupName;
    }

    public String getLogStreamPrefix() {
        return logStreamPrefix;
    }

    public String getLogStreamName() {
        // logStreamPrefix-YYYY-MM-DD
        return logStreamPrefix + "-" + new java.sql.Date(System.currentTimeMillis());
    }

    public CloudWatchAppender() {

        client = CloudWatchLogsClient.builder()
                .build();

        eventQueue = new LinkedList<>();
    }

    @Override
    protected void append(ILoggingEvent event) {

        if (logGroupName == null || logGroupName.isEmpty()) {
            throw new IllegalArgumentException("logGroupName must be set");
        }

        if (logStreamPrefix == null || logStreamPrefix.isEmpty()) {
            throw new IllegalArgumentException("logStreamPrefix must be set");
        }

        // Construct the log message
        InputLogEvent logEvent = InputLogEvent.builder()
                .message(event.getLevel().levelStr + " " + event.getFormattedMessage())
                .timestamp(event.getTimeStamp())
                .build();

        // Add event to the queue
        eventQueue.add(logEvent);

        // Flush queue if it has more than 10 events - P
        // if (eventQueue.size() >= 10) {
        // flushEvents();
        // }

        flushEvents();
    }

    private void flushEvents() {

        createLogGroupIfNotExists(logGroupName);
        // Construct the log stream name based on today's date
        String logStreamName = getLogStreamName();

        createLogStreamIfNotExists(logStreamName);

        // Retrieve the existing log events
        DescribeLogStreamsResponse describeLogStreamsResponse = client
                .describeLogStreams(DescribeLogStreamsRequest.builder()
                        .logGroupName(logGroupName)
                        .logStreamNamePrefix(logStreamName)
                        .build());

        String sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();

        // Batch up the next 10 events
        LinkedList<InputLogEvent> logEventsBatch = new LinkedList<>();
        while (!eventQueue.isEmpty() && logEventsBatch.size() < 10) {
            logEventsBatch.add(eventQueue.poll());
        }

        // Check if logEventsBatch is empty
        if (logEventsBatch.isEmpty()) {
            return; // Skip the API call if there are no log events
        }

        // Put the log events into the CloudWatch stream
        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(logEventsBatch)
                .sequenceToken(sequenceToken)
                .build();

        client.putLogEvents(putLogEventsRequest);
    }

    @Override
    public void stop() {
        // Flush any remaining events before stopping
        flushEvents();

        // Clean up the AWS CloudWatchLogs client
        client.close();

        super.stop();
    }

    private void createLogGroupIfNotExists(String logGroupName) {
        // Check if the log group exists
        if (client.describeLogGroups().logGroups().stream()
                .noneMatch(g -> g.logGroupName().equals(logGroupName))) {
            // Create the log group if it doesn't exist
            client.createLogGroup(r -> r.logGroupName(logGroupName));
        }
    }

    private void createLogStreamIfNotExists(String logStreamName) {
        // Check if the log stream exists
        if (client.describeLogStreams(DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamNamePrefix(logStreamName)
                .build()).logStreams().isEmpty()) {
            // Create the log stream if it doesn't exist
            client.createLogStream(r -> r.logGroupName(logGroupName).logStreamName(logStreamName));
        }
    }

}
