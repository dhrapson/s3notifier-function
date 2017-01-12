package com.wtr.s3notifier.email;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wtr.s3notifier.EnvVars;

public class EmailManagerIT {

	@BeforeClass
	public static void check() throws Exception {
		EnvVars.checkEnvVars(EnvVars.SMTP_USERNAME_ENV_VAR, EnvVars.SMTP_PASSWORD_ENV_VAR, EnvVars.EMAIL_FROM_ENV_VAR, EnvVars.EMAIL_TO_ENV_VAR);
	}
	
	@Test
    public void testS3BucketHandler() throws Exception {
	        
		EmailManager manager = new EmailManager("email-smtp.eu-west-1.amazonaws.com", 25, System.getenv(EnvVars.SMTP_USERNAME_ENV_VAR), System.getenv(EnvVars.SMTP_PASSWORD_ENV_VAR), System.getenv(EnvVars.EMAIL_FROM_ENV_VAR));
		manager.sendEmail(System.getenv("EMAIL_TO"), "the subject", "the body");
    }
}
