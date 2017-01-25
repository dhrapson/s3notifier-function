package com.wtr.s3notifier;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ClientDataFile {

    private final static String INPUT_DATA_FILE_IDENTIFIER = "/INPUT/";
    private String uploadPrefix, integratorId, clientId, fileName, fullId;
    private LocalDate received;

    public ClientDataFile(String uploadPrefix, String integratorId, String key, Date input) {
        super();
        this.uploadPrefix = uploadPrefix;
        this.integratorId = integratorId;
        this.clientId = clientNameFromKey(key);
        this.fileName = fileNameFromKey(key);
        this.fullId = key;
        this.received = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public String getIntegratorId() {
        return integratorId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUploadLocation() {
        return uploadPrefix + "/" + integratorId + "/" + clientId + "/" + insertDateIntoFileName(fileName);
    }

    public String getDownloadLocation() {
        return fullId;
    }

    public String getProcessedLocation() {
        String prefix = null;
        if (isThisInputFile()) {
            prefix = fullId.replaceFirst("/INPUT/", "/PROCESSED/");
        } else {
            prefix = clientId + "/PROCESSED/" + fileName;
        }
        return insertDateIntoFileName(prefix);
    }

    @Override
    public String toString() {
        return integratorId + "/" + fullId;
    }

    public boolean isThisInputFile() {
        return isInputFile(fullId);
    }

    public static boolean isInputFile(String key) {
        if (key.contains(INPUT_DATA_FILE_IDENTIFIER) && !key.endsWith(INPUT_DATA_FILE_IDENTIFIER)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
        result = prime * result + ((fullId == null) ? 0 : fullId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientDataFile other = (ClientDataFile) obj;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
        if (fullId == null) {
            if (other.fullId != null)
                return false;
        } else if (!fullId.equals(other.fullId))
            return false;
        return true;
    }

    private String insertDateIntoFileName(String filePath) {
        int finalDotIndex = filePath.lastIndexOf('.');
        if (finalDotIndex > 0) {
            return filePath.substring(0, finalDotIndex) + "-" + received + filePath.substring(finalDotIndex);
        }
        return filePath + "-" + received;
    }

    private String clientNameFromKey(String s3Key) {
        return s3Key.split("/")[0];
    }

    private String fileNameFromKey(String s3Key) {
        String file = s3Key.substring(s3Key.indexOf('/') + 1);
        file = file.replaceFirst("INPUT/", "");
        return file;
    }
}
