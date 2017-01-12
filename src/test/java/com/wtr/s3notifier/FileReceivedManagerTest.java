package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.Mockito;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class FileReceivedManagerTest {
	
	@Test
	public void testSingleFileProcessing() {
		EmailManager emailer = Mockito.mock(EmailManager.class);
		S3FileManager s3 = Mockito.mock(S3FileManager.class);
		DropboxManager dropbox = Mockito.mock(DropboxManager.class);
		FileReceivedManager manager = new FileReceivedManager(s3, dropbox, emailer, "test@example.com");
		ClientDataFile cdf = new ClientDataFile("/parent", "integrator", "client/INPUT/file");
		File f = new File("notused");
		when(s3.downloadFile("integrator", "client/INPUT/file")).thenReturn(f);
		
		assertEquals(true, manager.process(cdf));
		verify(dropbox).uploadFile(f, "/parent/integrator/client/file");
		verify(emailer).sendEmail("test@example.com", "A new integrator file has arrived for client", "The file is in Dropbox under /parent/integrator/client/file");
	}
}
