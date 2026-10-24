package com.example.monitoringservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring")
public record MonitoringProperties(String cron, String backendUrl, Mail mail) {
    public record Mail(String to) {}
}
