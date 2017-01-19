package com.wtr.s3notifier.s3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class S3FileTest {

    @Test
    public void testMatches() {
        S3File file = new S3File("bucket", "key", new Date());
        assertTrue(file.matches("bucket", "k(.*)"));
        assertFalse(file.matches("bucket", "p(.*)"));
    }
}
