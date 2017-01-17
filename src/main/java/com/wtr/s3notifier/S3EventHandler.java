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

public class S3EventHandler implements RequestHandler<SNSEvent, List<String>> {

    private static final Logger log = LogManager.getLogger(S3EventHandler.class);
    
    private FileReceivedManager manager;
    private Configurator config;
    private String dropboxParentFolder;
    
    @Override
    public List<String> handleRequest(SNSEvent input, Context context) {
    	
    	S3EventNotification s3Event = null;
    	
    	List<String> returnValues = new ArrayList<>();
    	for (SNSRecord record : input.getRecords()) {
    		String message = record.getSNS().getMessage();
    		s3Event = S3Event.parseJson(message);
    		returnValues.addAll(handleS3Request(s3Event, context));
    	}
    	return returnValues;
    }
    
    public List<String> handleS3Request(S3EventNotification input, Context context) {
    	
    	List<ClientDataFile> filesToProcess = new ArrayList<>();
    	List<String> returnValues = new ArrayList<>();
    	for (S3EventNotificationRecord record : input.getRecords()) {
    	    String s3Key = record.getS3().getObject().getKey();
    	    String s3Bucket = record.getS3().getBucket().getName();
    	    if (ClientDataFile.isInputFile(s3Key)) {
    	    	log.info("processing "+s3Key+" in "+s3Bucket);
    	    	filesToProcess.add(new ClientDataFile(getDropboxParentFolder(), s3Bucket, s3Key, new Date()));
    	    }
     	}
    	
    	for (ClientDataFile fileToProcess : filesToProcess) {
    		getFileReceivedManager().process(fileToProcess);
    		returnValues.add(fileToProcess.toString());
    	}
    	return returnValues;
    }
    
    String getDropboxParentFolder() {
    	if (dropboxParentFolder == null) {
	    	dropboxParentFolder = getConfigurator().getConfigValue("DROPBOX_PARENT_FOLDER");
	    	if (!dropboxParentFolder.startsWith("/")) {
	    		throw new ConfigurationException("Dropbox parent folder must start with a leading slash. The value provided does not: "+dropboxParentFolder);
	    	}
    	}
    	return dropboxParentFolder;
    }
    
    FileReceivedManager getFileReceivedManager() {
    	if (manager == null) {
	    	config = getConfigurator();
	    	String smtpHost = config.getConfigValue("SMTP_HOST");    	
	    	int smtpPort = Integer.parseInt(config.getConfigValue("SMTP_PORT"));
	    	String smtpUsername = config.getConfigValue("SMTP_USERNAME");    	
	    	String smtpPassword = config.getConfigValue("SMTP_PASSWORD");
	    	String emailFrom = config.getConfigValue("EMAIL_FROM");
	    	String emailTo = config.getConfigValue("EMAIL_TO");
	    	String dropboxAccessToken = config.getConfigValue("DROPBOX_ACCESS_TOKEN");
	    	
	    	manager = new FileReceivedManager( new S3FileManager(new AmazonS3Client()), 
	    			new DropboxManager(DropboxManager.getClient(dropboxAccessToken)), 
	    			new EmailManager(smtpHost, smtpPort, smtpUsername, smtpPassword, emailFrom),
	    			emailTo);
    	}
    	return manager;
    }
    
    Configurator getConfigurator() {
    	if (config == null) {
    		config = new Configurator();
    	}
    	return config;
    }

}
