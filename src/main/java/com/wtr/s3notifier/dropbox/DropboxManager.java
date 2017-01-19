package com.wtr.s3notifier.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestConfig.Builder;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxManager {

    private static final Logger log = LogManager.getLogger(DropboxManager.class);

    private DbxClientV2 dbxClient;

    public DropboxManager(DbxClientV2 dbxClient) {
        this.dbxClient = dbxClient;
    }

    public void uploadFile(File localFile, String remoteFilePath) {

        log.info("Uploading " + localFile.getPath() + " to dropbox: " + remoteFilePath);
        try (InputStream in = getInputStream(localFile)) {
            FileMetadata metadata = dbxClient.files().uploadBuilder(remoteFilePath).withMode(WriteMode.ADD).withClientModified(new Date(localFile.lastModified())).uploadAndFinish(in);

            log.info(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            throw new DropboxException(ex);
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            throw new DropboxException(ex);
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
            throw new DropboxException(ex);
        }
    }

    public void deleteRemoteFile(String remoteFilePath) {
        try {
            dbxClient.files().delete(remoteFilePath);
        } catch (DeleteErrorException e) {
            throw new DropboxException(e);
        } catch (DbxException e) {
            throw new DropboxException(e);
        }
    }

    public void downloadFile(String remoteFilePath, File localFile) {
        try (OutputStream outputStream = new FileOutputStream(localFile)) {
            dbxClient.files().download(remoteFilePath).download(outputStream);
        } catch (FileNotFoundException e) {
            throw new DropboxException(e);
        } catch (IOException e) {
            throw new DropboxException(e);
        } catch (DownloadErrorException e) {
            throw new DropboxException(e);
        } catch (DbxException e) {
            throw new DropboxException(e);
        }
    }

    public static DbxClientV2 getClient(String accessToken) {
        Builder newBuilder = DbxRequestConfig.newBuilder("Reaper/0.1");
        DbxRequestConfig config = newBuilder.withUserLocaleFrom(Locale.getDefault()).build();
        return new DbxClientV2(config, accessToken);
    }

    InputStream getInputStream(File localFile) throws FileNotFoundException {
        return new FileInputStream(localFile);
    }

}
