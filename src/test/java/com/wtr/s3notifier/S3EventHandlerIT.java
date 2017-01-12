package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class S3EventHandlerIT {
	
	 private static S3Event input;

	    @BeforeClass
	    public static void createInput() throws IOException {
	        input = TestUtils.parse("s3-event.put.json", S3Event.class);
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
	        List<String> expected = Arrays.asList("test-integrator/test-client/INPUT/test-file.csv");
	        assertEquals(expected, output);
	    }
	    
	    private Context createContext() {
	        TestContext ctx = new TestContext();
	        ctx.setFunctionName("S3Notifier");
	        return ctx;
	    }
}
