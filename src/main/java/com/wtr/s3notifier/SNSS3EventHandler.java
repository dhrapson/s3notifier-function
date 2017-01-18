package com.wtr.s3notifier;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class SNSS3EventHandler implements RequestHandler<SNSEvent, List<String>> {

    S3EventHandler handler;
    @Override
    public List<String> handleRequest(SNSEvent input, Context context) {
    	
    	S3EventNotification s3Event = null;
    	
    	List<String> returnValues = new ArrayList<>();
    	for (SNSRecord record : input.getRecords()) {
    		String message = record.getSNS().getMessage();
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
