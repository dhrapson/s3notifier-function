package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3File;
import com.wtr.s3notifier.s3.S3FileManager;
import com.wtr.s3notifier.s3.S3FileSet;

public class FileReceivedManagerTest {
	
	@Test
	public void testSingleFileProcessingForNonEmptyFile() throws Exception {
		EmailManager emailer = Mockito.mock(EmailManager.class);
		S3FileManager s3 = Mockito.mock(S3FileManager.class);
		DropboxManager dropbox = Mockito.mock(DropboxManager.class);
		FileReceivedManager managerReal = new FileReceivedManager(s3, dropbox, emailer, "test@example.com");
		FileReceivedManager managerSpy = Mockito.spy(managerReal);
		ClientDataFile cdf = new ClientDataFile("/parent", "integrator", "client/INPUT/file", new Date());
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
		ClientDataFile cdf = new ClientDataFile("/parent", "integrator", "client/INPUT/file", new Date());
		File f = new File("notused");
		Mockito.doReturn("  ").when(managerSpy).readFile(f);
		when(s3.downloadFile("integrator", "client/INPUT/file")).thenReturn(f);
		
		assertEquals(false, managerSpy.process(cdf));
	}
	
	@Test
	public void testProcessAll() throws Exception {
		
		S3FileManager s3 = Mockito.mock(S3FileManager.class);
		FileReceivedManager managerReal = new FileReceivedManager(s3, null, null, "test@example.com");
		FileReceivedManager managerSpy = Mockito.spy(managerReal);
		Date lastMod = new Date();
		when(s3.listBucketNames()).thenReturn(Arrays.asList("one", "two"));
		when(s3.listFiles("one")).thenReturn(new S3FileSet(Arrays.asList(new S3File("one", "client/PROCESSED/keyA", new Date()))));
		when(s3.listFiles("two")).thenReturn(new S3FileSet(Arrays.asList(new S3File("two", "client/INPUT/keyA", new Date()), new S3File("two", "client/PROCESSED/keyB", lastMod), new S3File("two", "client/PROCESSED/keyC", lastMod))));
		
		ClientDataFile twoA = new ClientDataFile("/foo", "two", "client/INPUT/keyA", lastMod);
		doReturn(true).when(managerSpy).process(twoA);
		assertEquals(1, managerSpy.processAll("/foo").size());		
		verify(managerSpy).process(eq(twoA));
	}
}
