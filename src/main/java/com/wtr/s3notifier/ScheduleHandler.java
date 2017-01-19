package com.wtr.s3notifier;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.wtr.s3notifier.email.EmailManager;
import com.wtr.s3notifier.s3.S3FileManager;

public class ScheduleHandler implements RequestHandler<String, List<String>> {

    private FileReceivedManager manager;
    private Configurator config;

    @Override
    public List<String> handleRequest(String input, Context context) {
        return getFileReceivedManager().warnOnUnmet(getCheckDate());
    }

    FileReceivedManager getFileReceivedManager() {
        if (manager == null) {
            config = getConfigurator();
            String smtpHost = config.getConfigValue("SMTP_HOST");
            int smtpPort = Integer.parseInt(config.getConfigValue("SMTP_PORT"));
            String smtpUsername = config.getConfigValue("SMTP_USERNAME");
            String smtpPassword = config.getConfigValue("SMTP_PASSWORD");
            String emailFrom = config.getConfigValue("EMAIL_FROM");
            String emailTo = config.getConfigValue("EMAIL_TO");

            manager = new FileReceivedManager(new S3FileManager(new AmazonS3Client()), null, new EmailManager(smtpHost, smtpPort, smtpUsername, smtpPassword, emailFrom), emailTo);
        }
        return manager;
    }

    Configurator getConfigurator() {
        if (config == null) {
            config = new Configurator();
        }
        return config;
    }

    LocalDate getCheckDate() {
        return LocalDate.now().minus(Period.ofDays(1));
    }

}
