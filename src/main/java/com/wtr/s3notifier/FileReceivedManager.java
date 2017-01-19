package com.wtr.s3notifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wtr.s3notifier.dropbox.DropboxManager;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3Exception;
import com.wtr.s3notifier.s3.S3File;
import com.wtr.s3notifier.s3.S3FileManager;
import com.wtr.s3notifier.s3.S3FileSet;

public class FileReceivedManager {

    private static final String DAILY_SCHEDULE_FILE_NAME = "DAILY_SCHEDULE";
    private static final String PROCESSED_FILES_KEY_ID = "PROCESSED";
    private static final Logger log = LogManager.getLogger(FileReceivedManager.class);

    private String fileProcessorEmailTo;
    private S3FileManager s3;
    private DropboxManager dropbox;
    private EmailManager emailer;

    public FileReceivedManager(S3FileManager s3, DropboxManager dropbox, EmailManager emailer, String fileProcessorEmailTo) {
        this.s3 = s3;
        this.dropbox = dropbox;
        this.emailer = emailer;
        this.fileProcessorEmailTo = fileProcessorEmailTo;
    }

    public List<String> processAll(String dropboxParentFolder) {

        List<ClientDataFile> filesToProcess = new ArrayList<>();
        for (String bucketName : s3.listBucketNames()) {
            for (S3File file : s3.listFiles(bucketName)) {
                if (ClientDataFile.isInputFile(file.getKey())) {
                    filesToProcess.add(new ClientDataFile(dropboxParentFolder, bucketName, file.getKey(), file.getLastModified()));
                }
            }
        }

        filesToProcess.stream().forEach(f -> process(f));

        return filesToProcess.stream().map(ClientDataFile::toString).sorted(String::compareTo).collect(Collectors.toList());
    }

    public List<String> warnOnUnmet(LocalDate date) {

        List<S3File> unMetDailySchedules = new ArrayList<>();

        for (String bucketName : s3.listBucketNames()) {
            S3FileSet set = s3.listFiles(bucketName);
            for (S3File file : set) {
                if (file.getKey().endsWith(DAILY_SCHEDULE_FILE_NAME)) {
                    String regex = file.getKey().replaceAll(DAILY_SCHEDULE_FILE_NAME, PROCESSED_FILES_KEY_ID) + "/(.*)" + date;
                    if (set.subsetMatching(file.getBucket(), regex).size() == 0) {
                        unMetDailySchedules.add(file);
                    }
                }
            }
        }

        unMetDailySchedules
                .forEach(file -> emailer.sendEmail(fileProcessorEmailTo, "No file received as per schedule: " + file.getPath(), "The file was expected on " + date + " but was not received."));

        log.info("Found " + unMetDailySchedules.size() + " unmet");
        return unMetDailySchedules.stream().map(S3File::toString).collect(Collectors.toList());
    }

    public boolean process(ClientDataFile cdf) {
        if (!cdf.isThisInputFile()) {
            log.info("not an input file: " + cdf);
            return false;
        }

        log.info("processing " + cdf);
        File file = s3.downloadFile(cdf.getIntegratorId(), cdf.getDownloadLocation());
        try {
            if (readFile(file).trim().length() == 0) {
                return false;
            }
        } catch (IOException e) {
            throw new S3Exception(e);
        }
        String targetLocation = cdf.getUploadLocation();
        dropbox.uploadFile(file, targetLocation);
        s3.moveFile(cdf.getIntegratorId(), cdf.getDownloadLocation(), cdf.getProcessedLocation());
        emailer.sendEmail(fileProcessorEmailTo, "A new " + cdf.getIntegratorId() + " file has arrived for " + cdf.getClientId(), "The file is in Dropbox under " + targetLocation);
        return true;

    }

    String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded);
    }

}
