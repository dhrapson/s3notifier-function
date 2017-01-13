package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDate;

import org.junit.Test;
import org.mockito.Mockito;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class FileReceivedManagerTest {
	
	@Test
	public void testSingleFileProcessingForNonEmptyFile() throws Exception {
		EmailManager emailer = Mockito.mock(EmailManager.class);
		S3FileManager s3 = Mockito.mock(S3FileManager.class);
		DropboxManager dropbox = Mockito.mock(DropboxManager.class);
		FileReceivedManager managerReal = new FileReceivedManager(s3, dropbox, emailer, "test@example.com");
		FileReceivedManager managerSpy = Mockito.spy(managerReal);
		ClientDataFile cdf = new ClientDataFile("/parent", "integrator", "client/INPUT/file");
		File f = new File("notused");
		when(s3.downloadFile("integrator", "client/INPUT/file")).thenReturn(f);
		Mockito.doReturn("somecontent").when(managerSpy).readFile(f);
		
		assertEquals(true, managerSpy.process(cdf));
		verify(dropbox).uploadFile(f, "/parent/integrator/client/file-"+LocalDate.now());
		verify(s3).moveFile("integrator", "client/INPUT/file", "client/PROCESSED/file-"+LocalDate.now());
		verify(emailer).sendEmail("test@example.com", "A new integrator file has arrived for client", "The file is in Dropbox under /parent/integrator/client/file-"+LocalDate.now());
	}
	
	@Test
	public void testSingleFileProcessingForEmptyFile() throws Exception {
		
		S3FileManager s3 = Mockito.mock(S3FileManager.class);
		FileReceivedManager managerReal = new FileReceivedManager(s3, null, null, "test@example.com");
		FileReceivedManager managerSpy = Mockito.spy(managerReal);
		ClientDataFile cdf = new ClientDataFile("/parent", "integrator", "client/INPUT/file");
		File f = new File("notused");
		Mockito.doReturn("  ").when(managerSpy).readFile(f);
		when(s3.downloadFile("integrator", "client/INPUT/file")).thenReturn(f);
		
		assertEquals(false, managerSpy.process(cdf));
	}
}
