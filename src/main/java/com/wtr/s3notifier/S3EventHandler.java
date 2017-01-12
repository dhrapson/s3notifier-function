package com.wtr.s3notifier;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class S3EventHandler implements RequestHandler<S3Event, List<String>> {

    private static final Logger log = LogManager.getLogger(S3EventHandler.class);
    
    private FileReceivedManager manager;
    private Configurator config;
    private String dropboxParentFolder;
    @Override
    public List<String> handleRequest(S3Event input, Context context) {
    	
    	manager = getFileReceivedManager();
    	
    	List<ClientDataFile> filesToProcess = new ArrayList<>();
    	List<String> returnValues = new ArrayList<>();
    	 
    	for (S3EventNotificationRecord record : input.getRecords()) {
    	    String s3Key = record.getS3().getObject().getKey();
    	    String s3Bucket = record.getS3().getBucket().getName();
    	    if (s3Key.contains("/INPUT/")) {
    	    	log.info("processing "+s3Key+" in "+s3Bucket);
    	    	filesToProcess.add(new ClientDataFile(dropboxParentFolder, s3Bucket, s3Key));
    	    }
     	}
    	
    	for (ClientDataFile fileToProcess : filesToProcess) {
    		manager.process(fileToProcess);
    		returnValues.add(fileToProcess.toString());
    	}
    	return returnValues;
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
	    	dropboxParentFolder = config.getConfigValue("DROPBOX_PARENT_FOLDER");
	    	manager = new FileReceivedManager( new S3FileManager(), 
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
