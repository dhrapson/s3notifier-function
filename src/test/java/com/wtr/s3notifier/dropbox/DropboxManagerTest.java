package com.wtr.s3notifier.dropbox;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxManagerTest {
	
	@Test
	public void testFileUpload() {
		
		String remoteFilePath = "/blah";
		DbxClientV2 dbxClient = Mockito.mock(DbxClientV2.class);
		DbxUserFilesRequests files =  Mockito.mock(DbxUserFilesRequests.class);
		UploadBuilder builder = Mockito.mock(UploadBuilder.class);
		when(dbxClient.files()).thenReturn(files);
		when(files.uploadBuilder(remoteFilePath)).thenReturn(builder);
		when(builder.withMode(any())).thenReturn(builder);
		when(builder.withClientModified(any())).thenReturn(builder);
				
//		InputStream is = new FileInputStream("c://filename");

//	    is.close(); 

		
	}
}
