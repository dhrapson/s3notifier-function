package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class S3EventHandlerTest {
	
	 private static S3Event input;

	    @BeforeClass
	    public static void createInput() throws IOException {
	        input = TestUtils.parse("s3-event.put.json", S3Event.class);
	    }

	    @Test
	    public void testLambdaFunctionHandler() {
	        S3EventHandler handlerReal = new S3EventHandler();
	        S3EventHandler handlerSpy = Mockito.spy(handlerReal);
	        	        
	        FileReceivedManager manager = Mockito.mock(FileReceivedManager.class);
	        doReturn(manager).when(handlerSpy).getFileReceivedManager();
	        
	        Context ctx = createContext();
	        
	        List<String> output = handlerSpy.handleRequest(input, ctx);
	        List<String> expected = Arrays.asList("test-integrator/test-client/INPUT/test-file.csv");
	        assertEquals(expected, output);
	        verify(manager).process(new ClientDataFile("test parent", "test-integrator", "test-client/INPUT/test-file.csv"));
	    }
	    
	    private Context createContext() {
	        TestContext ctx = new TestContext();
	        ctx.setFunctionName("S3Notifier");
	        return ctx;
	    }
}
