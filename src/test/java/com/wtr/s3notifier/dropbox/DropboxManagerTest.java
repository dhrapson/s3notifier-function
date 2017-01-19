package com.wtr.s3notifier.dropbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;

public class DropboxManagerTest {

    @Test
    public void testFileUpload() throws Exception {

        String remoteFilePath = "/blah";
        DbxClientV2 dbxClient = Mockito.mock(DbxClientV2.class);
        DropboxManager managerReal = new DropboxManager(dbxClient);
        DropboxManager managerSpy = Mockito.spy(managerReal);

        DbxUserFilesRequests files = Mockito.mock(DbxUserFilesRequests.class);
        InputStream mockIS = Mockito.mock(InputStream.class);
        UploadBuilder builder = Mockito.mock(UploadBuilder.class);
        FileMetadata mockMD = Mockito.mock(FileMetadata.class);
        File mockFile = Mockito.mock(File.class);

        Mockito.doReturn(mockIS).when(managerSpy).getInputStream(mockFile);
        when(dbxClient.files()).thenReturn(files);
        when(files.uploadBuilder(remoteFilePath)).thenReturn(builder);
        when(builder.withMode(any())).thenReturn(builder);
        when(builder.withClientModified(any())).thenReturn(builder);
        when(builder.uploadAndFinish(mockIS)).thenReturn(mockMD);

        managerSpy.uploadFile(mockFile, remoteFilePath);

    }
}
