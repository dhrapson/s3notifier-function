package com.wtr.s3notifier.email;

import org.junit.BeforeClass;
import org.junit.Test;

public class EmailManagerIT {

	private static final String SMTP_USERNAME_ENV_VAR = "SMTP_USERNAME";
	private static final String SMTP_PASSWORD_ENV_VAR = "SMTP_PASSWORD";
	private static final String EMAIL_FROM_ENV_VAR = "EMAIL_FROM";
	private static final String EMAIL_TO_ENV_VAR = "EMAIL_TO";
	
	@BeforeClass
	public static void check() throws Exception {
		String[] envVars = {SMTP_USERNAME_ENV_VAR, SMTP_PASSWORD_ENV_VAR, EMAIL_FROM_ENV_VAR, EMAIL_TO_ENV_VAR};
		for (String envVar : envVars) {
			if (System.getenv(envVar) == null) {
				throw new Exception("Missing environment variable: "+envVar);
			}
		}
	}
	
	@Test
    public void testS3BucketHandler() throws Exception {
	        
		EmailManager manager = new EmailManager("email-smtp.eu-west-1.amazonaws.com", 25, System.getenv(SMTP_USERNAME_ENV_VAR), System.getenv(SMTP_PASSWORD_ENV_VAR), System.getenv(EMAIL_FROM_ENV_VAR));
		manager.sendEmail(System.getenv("EMAIL_TO"), "the subject", "the body");
    }
}
