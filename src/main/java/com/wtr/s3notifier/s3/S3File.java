package com.wtr.s3notifier.s3;

import java.util.Date;
import java.util.regex.Pattern;

public class S3File {

    private String bucket, key;
    private Date lastModified;

    public S3File(String bucket, String key, Date lastModified) {
        super();
        this.bucket = bucket;
        this.key = key;
        this.lastModified = lastModified;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public boolean matches(String bucketName, String keyRegex) {
        if (bucket != bucketName) {
            return false;
        }
        return Pattern.matches(keyRegex, key);
    }

    public String getPath() {
        return "/" + bucket + "/" + key;
    }

    @Override
    public String toString() {
        return "S3File [bucket=" + bucket + ", key=" + key + ", lastModified=" + lastModified + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bucket == null) ? 0 : bucket.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
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
        S3File other = (S3File) obj;
        if (bucket == null) {
            if (other.bucket != null)
                return false;
        } else if (!bucket.equals(other.bucket))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (lastModified == null) {
            if (other.lastModified != null)
                return false;
        } else if (!lastModified.equals(other.lastModified))
            return false;
        return true;
    }

}
