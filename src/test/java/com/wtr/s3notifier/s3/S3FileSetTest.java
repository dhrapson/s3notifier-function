package com.wtr.s3notifier.s3;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

public class S3FileSetTest {

    @Test
    public void testSubsetMatching() {
        S3FileSet set = new S3FileSet(Arrays.asList(new S3File("bucket1", "key1", new Date()), new S3File("bucket1", "key2", new Date())));
        assertEquals(set.subsetMatching("bucket1", "key(.*)").size(), 2);
        assertEquals(set.subsetMatching("bucket1", "key1(.*)").size(), 1);
        assertEquals(set.subsetMatching("bucket1", "foo(.*)").size(), 0);
    }
}
