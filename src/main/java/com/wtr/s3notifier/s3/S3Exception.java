package com.wtr.s3notifier.s3;

public class S3Exception extends RuntimeException {

    private static final long serialVersionUID = 6217664656747701530L;

    public S3Exception(Exception e) {
        super(e);
    }
}
