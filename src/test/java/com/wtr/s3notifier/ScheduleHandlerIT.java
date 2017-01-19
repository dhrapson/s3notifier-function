package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3Client;
import com.wtr.s3notifier.s3.S3FileManager;

@RunWith(Enclosed.class)
public class ScheduleHandlerIT {

    @BeforeClass
    public static void createInput() throws Exception {
        EnvVars.checkEnvVars(EnvVars.SMTP_USERNAME_ENV_VAR, EnvVars.SMTP_PASSWORD_ENV_VAR, EnvVars.EMAIL_FROM_ENV_VAR, EnvVars.EMAIL_TO_ENV_VAR);
    }

    private static Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("ScheduleHandler");
        return ctx;
    }

    public static class TestAbsenceOfScheduleNoWarning {

        LocalDate testDate = LocalDate.parse("1970-01-01");

        @Before
        public void setup() {
            S3FileManager s3 = new S3FileManager(new AmazonS3Client());
            s3.deleteFile("test-integrator", "test-client/DAILY_SCHEDULE");
        }

        @Test
        public void testLambdaFunctionHandlerNoWarning() {
            ScheduleHandler handlerReal = new ScheduleHandler();
            ScheduleHandler handlerSpy = Mockito.spy(handlerReal);

            Configurator spyConfig = Mockito.spy(Configurator.class);
            doReturn("email-smtp.eu-west-1.amazonaws.com").when(spyConfig).getConfigValue("SMTP_HOST");
            doReturn("25").when(spyConfig).getConfigValue("SMTP_PORT");
            doReturn(spyConfig).when(handlerSpy).getConfigurator();
            doReturn(testDate).when(handlerSpy).getCheckDate();

            Context ctx = createContext();

            List<String> output = handlerSpy.handleRequest("foo", ctx);
            if (output == null) {
                System.out.println("output is null");
            }
            System.out.println("output is " + output);
            List<String> expected = Arrays.asList();
            if (expected == null) {
                System.out.println("expected is null");
            }
            System.out.println("expected is " + expected);
            assertEquals(expected, output);
        }
    }

    public static class TestProcessedFileNoWarning {

        LocalDate testDate = LocalDate.parse("1970-01-02");

        @Before
        public void setup() {
            S3FileManager s3 = new S3FileManager(new AmazonS3Client());
            URL resource = this.getClass().getResource("/upload-fixture.txt");
            File sourceFixture = new File(resource.getPath());
            s3.uploadFile("test-integrator", "test-client/DAILY_SCHEDULE", sourceFixture);
            s3.uploadFile("test-integrator", "test-client/PROCESSED/test-file.csv-" + testDate, sourceFixture);
        }

        @Test
        public void testLambdaFunctionHandlerNoWarning() {
            ScheduleHandler handlerReal = new ScheduleHandler();
            ScheduleHandler handlerSpy = Mockito.spy(handlerReal);

            Configurator spyConfig = Mockito.spy(Configurator.class);
            doReturn("email-smtp.eu-west-1.amazonaws.com").when(spyConfig).getConfigValue("SMTP_HOST");
            doReturn("25").when(spyConfig).getConfigValue("SMTP_PORT");
            doReturn(spyConfig).when(handlerSpy).getConfigurator();
            doReturn(testDate).when(handlerSpy).getCheckDate();

            Context ctx = createContext();

            List<String> output = handlerSpy.handleRequest("foo", ctx);
            List<String> expected = Arrays.asList();
            assertEquals(expected, output);
        }
    }

    public static class TestScheduleWarning {

        LocalDate testDate = LocalDate.parse("1970-01-03");

        @Before
        public void setup() {
            S3FileManager s3 = new S3FileManager(new AmazonS3Client());
            URL resource = this.getClass().getResource("/upload-fixture.txt");
            File sourceFixture = new File(resource.getPath());
            s3.uploadFile("test-integrator", "test-client/DAILY_SCHEDULE", sourceFixture);
        }

        @Test
        public void testLambdaFunctionHandlerWarning() {
            ScheduleHandler handlerReal = new ScheduleHandler();
            ScheduleHandler handlerSpy = Mockito.spy(handlerReal);

            Configurator spyConfig = Mockito.spy(Configurator.class);
            doReturn("email-smtp.eu-west-1.amazonaws.com").when(spyConfig).getConfigValue("SMTP_HOST");
            doReturn("25").when(spyConfig).getConfigValue("SMTP_PORT");
            doReturn(spyConfig).when(handlerSpy).getConfigurator();
            doReturn(testDate).when(handlerSpy).getCheckDate();

            Context ctx = createContext();

            List<String> output = handlerSpy.handleRequest("foo", ctx);
            List<String> expected = Arrays.asList("/test-integrator/test-client/DAILY_SCHEDULE");
            assertEquals(expected, output);
        }
    }

}
