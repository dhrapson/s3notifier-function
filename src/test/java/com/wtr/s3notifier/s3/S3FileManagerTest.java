package com.wtr.s3notifier.s3;

import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;

public class S3FileManagerTest {
	
	@Test
	public void testFileMove() {
		AmazonS3 mockS3 = Mockito.mock(AmazonS3Client.class);
		S3FileManager manager = new S3FileManager(mockS3);
		S3FileManager managerSpy = Mockito.spy(manager);
		CopyObjectRequest request = Mockito.mock(CopyObjectRequest.class);
		Mockito.doReturn(request).when(managerSpy).createCopyObjectRequest("bucket", "old", "new");
		managerSpy.moveFile("bucket", "old", "new");
				
		Mockito.verify(mockS3).copyObject(request);
		Mockito.verify(mockS3).deleteObject("bucket", "old");
	}
	
	@Test
	public void testCreateCopyObjRequest() {
		AmazonS3 mockS3 = Mockito.mock(AmazonS3Client.class);
		S3FileManager manager = new S3FileManager(mockS3);
		CopyObjectRequest request = manager.createCopyObjectRequest("bucket", "old", "new");
		assertEquals(request.getDestinationBucketName(), "bucket");
		assertEquals(request.getSourceBucketName(), "bucket");
		assertEquals(request.getSourceKey(), "old");
		assertEquals(request.getDestinationKey(), "new");
		assertEquals(request.getNewObjectMetadata().getSSEAlgorithm(), "AES256");
	}
}
