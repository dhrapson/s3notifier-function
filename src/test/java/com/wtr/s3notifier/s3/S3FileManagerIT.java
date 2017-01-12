package com.wtr.s3notifier.s3;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.wtr.s3notifier.s3.S3FileManager;

public class S3FileManagerIT {
	
	@Test
	public void testFileDownload() {
		String remoteFilePath = "/upload-fixture.txt";
		
		URL resource = this.getClass().getResource("/upload-fixture.txt");
		File sourceFixture = new File(resource.getPath());
		String bucketName = "test-integrator";
		S3FileManager manager = new S3FileManager();
		
		manager.uploadFile(bucketName, remoteFilePath, sourceFixture);
		
		String fileName = manager.listFiles(bucketName, remoteFilePath).get(0);
		File downloaded = manager.downloadFile(bucketName, fileName);
		assertNotNull(downloaded);
		
	}
}
