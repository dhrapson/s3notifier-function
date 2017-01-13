package com.wtr.s3notifier.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3Client;

public class S3FileManagerIT {
	
	@Test
	public void testFileDownload() {
		String remoteFilePath = "/upload-fixture.txt";
		
		URL resource = this.getClass().getResource("/upload-fixture.txt");
		File sourceFixture = new File(resource.getPath());
		String bucketName = "test-integrator";
		S3FileManager manager = new S3FileManager(new AmazonS3Client());
		
		manager.uploadFile(bucketName, remoteFilePath, sourceFixture);
		
		String fileName = manager.listFiles(bucketName, remoteFilePath).get(0);
		File downloaded = manager.downloadFile(bucketName, fileName);
		assertNotNull(downloaded);
		
		manager.deleteFile(bucketName, remoteFilePath);
	}
	
	@Test
	public void testFileMove() {
		String remoteFilePath = "FROM/move-fixture.txt";
		String movedFilePath = "TO/move-fixture.txt.2017-01-13";
		
		URL resource = this.getClass().getResource("/upload-fixture.txt");
		File sourceFixture = new File(resource.getPath());
		String bucketName = "test-integrator";
		S3FileManager manager = new S3FileManager(new AmazonS3Client());
		
		manager.uploadFile(bucketName, remoteFilePath, sourceFixture);
		
		manager.moveFile(bucketName, remoteFilePath, movedFilePath);
		String newFileName = manager.listFiles(bucketName, movedFilePath).get(0);
		assertNotNull(newFileName);
		assertEquals(manager.listFiles(bucketName, remoteFilePath).size(), 0);
		
		manager.deleteFile(bucketName, movedFilePath);
	}
}
