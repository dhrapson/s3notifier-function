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
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3FileManager {
	
	private static final Logger log = LogManager.getLogger(S3FileManager.class);
	
	private AmazonS3 s3Client;
	
	public S3FileManager(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}
		
	public List<String> listBucketNames() {
		List<Bucket> buckets = s3Client.listBuckets();
		List<String> bucketNames = new ArrayList<>();
		for (Bucket bucket : buckets) {
			bucketNames.add(bucket.getName());
		}
		return bucketNames;
	}
	
	public S3FileSet listFiles(String bucketName) {
		return listFiles(bucketName, null);
	}
	
	public S3FileSet listFiles(String bucketName, String prefix) {
		S3FileSet filesFound = new S3FileSet();

        try {
            log.info("Listing objects");
    		ListObjectsV2Result result;
    		
    		if (prefix != null) {
    			result = s3Client.listObjectsV2(bucketName, prefix);
            } else {
            	result = s3Client.listObjectsV2(bucketName);
            }
    		
    		for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
          	   filesFound.add(new S3File(bucketName, objectSummary.getKey(), objectSummary.getLastModified()));
            }
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
        return filesFound;
	}
	
	public void uploadFile(String s3Bucket, String s3Key, File file) {
		PutObjectRequest putRequest = new PutObjectRequest(
				s3Bucket, s3Key, file);

		// Request server-side encryption.
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

		putRequest.setMetadata(objectMetadata);
		s3Client.putObject(putRequest);
	    
	    log.info("Uploaded object from "+file.getPath()+" to "+s3Bucket+"/"+s3Key);   
	}
	
	public void moveFile(String s3Bucket, String oldKey, String newKey) {		
		s3Client.copyObject(createCopyObjectRequest(s3Bucket, oldKey, newKey));
	    
	    log.info("Copied object from "+oldKey+" to "+newKey+" in "+s3Bucket);
	    s3Client.deleteObject(s3Bucket, oldKey);
	    log.info("Deleted moved object from "+oldKey+" in "+s3Bucket);
	}
	
	public void deleteFile(String s3Bucket, String key) {
		s3Client.deleteObject(s3Bucket, key);
	   
	    log.info("Deleted object from "+key+" in "+s3Bucket);
	}
	
	
	public File downloadFile(String s3Bucket, String s3Key) {
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
	
	CopyObjectRequest createCopyObjectRequest(String s3Bucket, String oldKey, String newKey) {
		CopyObjectRequest copyObjRequest = new CopyObjectRequest(s3Bucket, oldKey, s3Bucket, newKey);

		// Request server-side encryption.
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

		copyObjRequest.setNewObjectMetadata(objectMetadata);
		
		return copyObjRequest;
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
