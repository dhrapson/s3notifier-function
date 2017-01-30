package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3Client;
import com.wtr.s3notifier.s3.S3FileManager;

public class S3EventHandlerIT {

    private static S3Event input;

    @BeforeClass
    public static void createInput() throws Exception {
        EnvVars.checkEnvVars(EnvVars.DROPBOX_PARENT_FOLDER_ENV_VAR, EnvVars.DROPBOX_ACCESS_TOKEN_ENV_VAR, EnvVars.SMTP_USERNAME_ENV_VAR, EnvVars.SMTP_PASSWORD_ENV_VAR, EnvVars.EMAIL_FROM_ENV_VAR,
                EnvVars.EMAIL_TO_ENV_VAR);
        input = TestUtils.parse("s3-event.put.json", S3Event.class);
    }

    @Before
    public void setup() {
        S3FileManager s3 = new S3FileManager(new AmazonS3Client());
        URL resource = this.getClass().getResource("/upload-fixture.txt");
        File sourceFixture = new File(resource.getPath());
        s3.uploadFile("wtrci", "otherclient/INPUT/test-file.csv", sourceFixture);
    }

    @Test
    public void testLambdaFunctionHandler() {
        S3EventHandler handlerReal = new S3EventHandler();
        S3EventHandler handlerSpy = Mockito.spy(handlerReal);

        Configurator spyConfig = Mockito.spy(Configurator.class);
        doReturn("email-smtp.eu-west-1.amazonaws.com").when(spyConfig).getConfigValue("SMTP_HOST");
        doReturn("25").when(spyConfig).getConfigValue("SMTP_PORT");
        doReturn(spyConfig).when(handlerSpy).getConfigurator();

        Context ctx = createContext();

        List<String> output = handlerSpy.handleRequest(input, ctx);
        List<String> expected = Arrays.asList("wtrci/otherclient/INPUT/test-file.csv");
        assertEquals(expected, output);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("S3Notifier");
        return ctx;
    }
}
