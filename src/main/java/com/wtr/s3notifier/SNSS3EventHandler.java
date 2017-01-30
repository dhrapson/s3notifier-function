package com.wtr.s3notifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.event.S3EventNotification;

public class SNSS3EventHandler implements RequestHandler<SNSEvent, List<String>> {

    private static final Logger log = LogManager.getLogger(SNSS3EventHandler.class);
    S3EventHandler handler;

    @Override
    public List<String> handleRequest(SNSEvent input, Context context) {

        S3EventNotification s3Event = null;

        List<String> returnValues = new ArrayList<>();
        for (SNSRecord record : input.getRecords()) {
            String message = record.getSNS().getMessage();
            log.info(message);
            s3Event = S3Event.parseJson(message);
            returnValues.addAll(getHandler().handleRequest(s3Event, context));
        }
        return returnValues;
    }

    private S3EventHandler getHandler() {
        if (handler == null) {
            handler = new S3EventHandler();
        }
        return handler;
    }
}
