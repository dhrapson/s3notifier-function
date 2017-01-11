package com.wtr.s3notifier;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;

public class S3EventHandler implements RequestHandler<S3Event, List<String>> {

    private static final Logger log = LogManager.getLogger(S3EventHandler.class);
	    
    public List<String> handleRequest(S3Event input, Context context) {
    	List<String> filesToProcess = new ArrayList<>();
    	 
    	for (S3EventNotificationRecord record : input.getRecords()) {
    	    String s3Key = record.getS3().getObject().getKey();
    	    String s3Bucket = record.getS3().getBucket().getName();
    	    if (s3Key.contains("/INPUT/")) {
    	    	log.info("processing "+s3Key+" in "+s3Bucket);
    	    	filesToProcess.add(s3Bucket+"::"+s3Key);
    	    } 
     	}
    	
    	return filesToProcess;
    }
}
