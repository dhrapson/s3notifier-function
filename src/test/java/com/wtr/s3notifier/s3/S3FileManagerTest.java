package com.wtr.s3notifier.s3;

import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3FileManagerTest {
	
	@Test
	public void testFileMove() {
		AmazonS3 mockS3 = Mockito.mock(AmazonS3Client.class);
		S3FileManager manager = new S3FileManager(mockS3);

		manager.moveFile("bucket", "old", "new");
		Mockito.verify(mockS3).copyObject("bucket", "old", "bucket", "new");
		Mockito.verify(mockS3).deleteObject("bucket", "old");
	}
}
