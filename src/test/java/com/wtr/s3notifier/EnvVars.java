package com.wtr.s3notifier;

public class EnvVars {

    public static final String DROPBOX_ACCESS_TOKEN_ENV_VAR = "DROPBOX_ACCESS_TOKEN";
    public static final String DROPBOX_PARENT_FOLDER_ENV_VAR = "DROPBOX_PARENT_FOLDER";

    public static void checkEnvVars(String... envVars) throws Exception {
        for (String envVar : envVars) {
            if (System.getenv(envVar) == null) {
                throw new Exception("Missing environment variable: " + EnvVars.DROPBOX_ACCESS_TOKEN_ENV_VAR);
            }
        }
    }

    public static final String SMTP_USERNAME_ENV_VAR = "SMTP_USERNAME";
    public static final String SMTP_PASSWORD_ENV_VAR = "SMTP_PASSWORD";
    public static final String EMAIL_FROM_ENV_VAR = "EMAIL_FROM";
    public static final String EMAIL_TO_ENV_VAR = "EMAIL_TO";
}
