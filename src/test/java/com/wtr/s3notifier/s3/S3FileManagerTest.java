package com.wtr.s3notifier.s3;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3FileManagerTest {
	
	@Test
	public void testListObjectsInBucket() {
		AmazonS3 mockS3 = mock(AmazonS3Client.class);
		S3FileManager manager = new S3FileManager(mockS3);
		ListObjectsV2Result result = mock(ListObjectsV2Result.class);
		Mockito.when(mockS3.listObjectsV2("bucket")).thenReturn(result);
		S3ObjectSummary mockSummary = mock(S3ObjectSummary.class);
		when(result.getObjectSummaries()).thenReturn(Arrays.asList(new S3ObjectSummary[] {mockSummary}));
		when(mockSummary.getKey()).thenReturn("thekey");
		when(mockSummary.getLastModified()).thenReturn(new Date(100000));
		
		assertEquals(manager.listFiles("bucket"), new HashSet<S3File>(Arrays.asList(new S3File("bucket","thekey", new Date(100000)))));
		
	}
	
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
