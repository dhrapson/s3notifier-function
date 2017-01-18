package com.wtr.s3notifier;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class ReaperHandler implements RequestHandler<String, List<String>> {
	
	private FileReceivedManager manager;
    private Configurator config;
    private String dropboxParentFolder;

	@Override
	public List<String> handleRequest(String input, Context context) {
		return getFileReceivedManager().processAll(getDropboxParentFolder());
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
	
	String getDropboxParentFolder() {
    	if (dropboxParentFolder == null) {
	    	dropboxParentFolder = getConfigurator().getConfigValue("DROPBOX_PARENT_FOLDER");
	    	if (!dropboxParentFolder.startsWith("/")) {
	    		throw new ConfigurationException("Dropbox parent folder must start with a leading slash. The value provided does not: "+dropboxParentFolder);
	    	}
    	}
    	return dropboxParentFolder;
    }
	
	Configurator getConfigurator() {
    	if (config == null) {
    		config = new Configurator();
    	}
    	return config;
    }
}
