package com.wtr.s3notifier.s3;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class S3FileSet extends HashSet<S3File> {

    private static final long serialVersionUID = -3843837084850833271L;

    public S3FileSet() {
        super();
    }

    public S3FileSet(Collection<? extends S3File> c) {
        super(c);
    }

    public S3FileSet subsetMatching(String bucketName, String keyRegex) {
        return new S3FileSet(this.stream().filter(f -> f.matches(bucketName, keyRegex)).collect(Collectors.toSet()));
    }
}
