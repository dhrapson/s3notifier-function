package com.wtr.s3notifier.dropbox;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wtr.s3notifier.s3.S3Exception;

public class DropboxManagerIT {
	
	private static final String ACCESS_TOKEN_ENV_VAR = "DROPBOX_ACCESS_TOKEN";
	
	@BeforeClass
	public static void check() throws Exception {
		if (System.getenv(ACCESS_TOKEN_ENV_VAR) == null) {
			throw new Exception("Missing environment variable: "+ACCESS_TOKEN_ENV_VAR);
		}
	}
	
	@Test
	public void testDropboxFileManagement() throws Exception {
		
		DropboxManager dropbox = new DropboxManager(DropboxManager.getClient(System.getenv(ACCESS_TOKEN_ENV_VAR)));
		String remoteFilePath = "/testing/upload-fixture.txt";
		try {
			URL resource = this.getClass().getResource("/upload-fixture.txt");
			File sourceFixture = new File(resource.getPath());
			dropbox.uploadFile(sourceFixture, remoteFilePath);
			File temp;
			try {
				temp = File.createTempFile("temp-file-name", ".tmp");
			} catch (IOException e) {
				throw new S3Exception(e);
			}
			dropbox.downloadFile( remoteFilePath, temp);
			byte[] f1 = Files.readAllBytes(sourceFixture.toPath());
			byte[] f2 = Files.readAllBytes(temp.toPath());
			assertArrayEquals(f1, f2);
		} finally {
			dropbox.deleteRemoteFile(remoteFilePath);
		}
	}
}
