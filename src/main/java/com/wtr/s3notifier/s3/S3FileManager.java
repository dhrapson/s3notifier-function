package com.wtr.s3notifier.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3FileManager {
	
	private static final Logger log = LogManager.getLogger(S3FileManager.class);
	
	public List<String> listFiles(String bucketName) {
		return listFiles(bucketName, null);
	}
	public List<String> listFiles(String bucketName, String prefix) {
		AmazonS3 s3client = new AmazonS3Client();
		List<String> objectNames = new ArrayList<>();
        try {
            log.info("Listing objects");
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
            if (prefix != null) {
            	req.withPrefix(prefix);
            }
            ListObjectsV2Result result;
            do {               
               result = s3client.listObjectsV2(req);
               
               for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                   objectNames.add(objectSummary.getKey());
               }
               log.debug("Next Continuation Token : " + result.getNextContinuationToken());
               req.setContinuationToken(result.getNextContinuationToken());
            } while(result.isTruncated() == true ); 
            
         } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, " +
            		"which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            log.debug("Error Message:    " + ase.getMessage());
            log.debug("HTTP Status Code: " + ase.getStatusCode());
            log.debug("AWS Error Code:   " + ase.getErrorCode());
            log.debug("Error Type:       " + ase.getErrorType());
            log.debug("Request ID:       " + ase.getRequestId());
            throw new S3Exception(ase);
        } catch (AmazonClientException ace) {
        	log.info("Caught an AmazonClientException, " +
            		"which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
        	log.error("Error Message: " + ace.getMessage());
            throw new S3Exception(ace);
        }
        return objectNames;
	}
	
	public void uploadFile(String s3Bucket, String s3Key, File file) {
		AmazonS3Client s3Client = new AmazonS3Client();        
				
		s3Client.putObject(new PutObjectRequest(s3Bucket, s3Key, file));
	    
	    log.info("Uploaded object from "+file.getPath()+" to "+s3Bucket+"/"+s3Key);   
	}
	
	public File downloadFile(String s3Bucket, String s3Key) {
		AmazonS3Client s3Client = new AmazonS3Client();        
				
		S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
	    InputStream objectData = object.getObjectContent();	    
	    
	    File temp;
		try {
			temp = File.createTempFile("s3-downloaded", ".tmp");
		} catch (IOException e) {
			throw new S3Exception(e);
		}
		copyInputStreamToFile(objectData, temp);
		log.info("Downloaded object from "+s3Bucket+"/"+s3Key+" and written to "+temp.getPath());
	    return temp;
	}
	
	private void copyInputStreamToFile( InputStream in, File file ) {
		OutputStream out = null;
	    try {
	        out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        
	    } catch (Exception e) {
	    	throw new S3Exception(e);
	    } finally {
	    	try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
}
