package com.wtr.s3notifier;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class FileReceivedManager {
	
	private static final Logger log = LogManager.getLogger(FileReceivedManager.class);
	
	private String fileProcessorEmailTo;
	private S3FileManager s3;
	private DropboxManager dropbox;
	private EmailManager emailer;
			
	public FileReceivedManager(S3FileManager s3, DropboxManager dropbox, EmailManager emailer, String fileProcessorEmailTo) {
		this.s3 = s3;
		this.dropbox = dropbox;
		this.emailer = emailer;
		this.fileProcessorEmailTo = fileProcessorEmailTo;
	}
	
	public boolean process(ClientDataFile cdf) {
		if (cdf.isThisInputFile()) {
	    	log.info("processing "+cdf.getFileName()+" in "+cdf.getClientId()+" in "+cdf.getIntegratorId());
	    	File file = s3.downloadFile(cdf.getIntegratorId(), cdf.getDownloadLocation());
	    	String targetLocation = cdf.getUploadLocation();
	    	dropbox.uploadFile(file, targetLocation);
	    	emailer.sendEmail(fileProcessorEmailTo, "A new "+cdf.getIntegratorId()+" file has arrived for "+cdf.getClientId(), "The file is in Dropbox under "+targetLocation);
	    	return true;
	    }
		return false;
	}
	

}
