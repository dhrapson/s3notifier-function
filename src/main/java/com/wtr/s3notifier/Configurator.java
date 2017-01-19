package com.wtr.s3notifier;

public class Configurator {

    public String getConfigValue(String key) {
        String value = System.getenv(key);
        if (value == null) {
            throw new ConfigurationException("No configuration value for " + key);
        }
        return value;
    }

}
