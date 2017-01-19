package com.wtr.s3notifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3Exception;
import com.wtr.s3notifier.s3.S3File;
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
	
	public List<String> processAll(String dropboxParentFolder) {

		List<ClientDataFile> filesToProcess = new ArrayList<>();		
		for (String bucketName : s3.listBucketNames()) {
			for (S3File file : s3.listFiles(bucketName)) {
				if (ClientDataFile.isInputFile(file.getKey())) {
					filesToProcess.add(new ClientDataFile(dropboxParentFolder, bucketName, file.getKey(), file.getLastModified()));
				}
			}
		}
	
		filesToProcess.stream().forEach(f -> process(f));
		
		return filesToProcess.stream().map(ClientDataFile::toString).collect(Collectors.toList());
	}
	
	public boolean process(ClientDataFile cdf) {
		if (!cdf.isThisInputFile()) {
			log.info("not an input file: "+cdf);
			return false;
		}
		
    	log.info("processing "+cdf);
    	File file = s3.downloadFile(cdf.getIntegratorId(), cdf.getDownloadLocation());
    	try {
			if (readFile(file).trim().length() == 0) {
				return false;
			}
		} catch (IOException e) {
			throw new S3Exception(e);
		}
    	String targetLocation = cdf.getUploadLocation();
    	dropbox.uploadFile(file, targetLocation);
    	s3.moveFile(cdf.getIntegratorId(), cdf.getDownloadLocation(), cdf.getProcessedLocation());
    	emailer.sendEmail(fileProcessorEmailTo, "A new "+cdf.getIntegratorId()+" file has arrived for "+cdf.getClientId(), "The file is in Dropbox under "+targetLocation);
    	return true;
			
	}
	
	String readFile(File file) throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(file.toPath());
	  return new String(encoded);
	}

}
