package com.wtr.s3notifier.dropbox;

public class DropboxException extends RuntimeException {

    private static final long serialVersionUID = 6609851626033396716L;

    public DropboxException(Exception ex) {
        super(ex);
    }

}
