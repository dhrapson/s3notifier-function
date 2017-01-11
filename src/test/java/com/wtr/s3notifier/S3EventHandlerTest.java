package com.wtr.s3notifier;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

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
	        S3EventHandler handler = new S3EventHandler();
	        Context ctx = createContext();
	        
	        List<String> output = handler.handleRequest(input, ctx);
	        List<String> expected = Arrays.asList("sourcebucket::myclient/INPUT/myfile.csv");
	        assertEquals(expected, output);
	    }
	    
	    private Context createContext() {
	        TestContext ctx = new TestContext();
	        ctx.setFunctionName("S3Notifier");
	        return ctx;
	    }
}
