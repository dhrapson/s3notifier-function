package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;

public class ClientDataFileTest {

    @Test
    public void testProcessedLocation() {
        ClientDataFile cdf = new ClientDataFile("/tst", "theintegrator", "theclient/thekey", new Date());
        String path = cdf.getProcessedLocation();
        assertEquals("theclient/PROCESSED/thekey-" + LocalDate.now(), path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/thekey.txt", new Date());
        path = cdf.getProcessedLocation();
        assertEquals("theclient/PROCESSED/thekey-" + LocalDate.now() + ".txt", path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/INPUT/thekey", new Date());
        path = cdf.getProcessedLocation();
        assertEquals("theclient/PROCESSED/thekey-" + LocalDate.now(), path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/INPUT/thekey.txt", new Date());
        path = cdf.getProcessedLocation();
        assertEquals("theclient/PROCESSED/thekey-" + LocalDate.now() + ".txt", path);
    }

    @Test
    public void testUploadLocation() {
        ClientDataFile cdf = new ClientDataFile("/tst", "theintegrator", "theclient/thekey", new Date());
        String path = cdf.getUploadLocation();
        assertEquals("/tst/theintegrator/theclient/thekey-" + LocalDate.now(), path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/INPUT/thekey", new Date());
        path = cdf.getUploadLocation();
        assertEquals("/tst/theintegrator/theclient/thekey-" + LocalDate.now(), path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/thekey.csv", new Date());
        path = cdf.getUploadLocation();
        assertEquals("/tst/theintegrator/theclient/thekey-" + LocalDate.now() + ".csv", path);

        cdf = new ClientDataFile("/tst", "theintegrator", "theclient/INPUT/thekey.csv", new Date());
        path = cdf.getUploadLocation();
        assertEquals("/tst/theintegrator/theclient/thekey-" + LocalDate.now() + ".csv", path);
    }
}
