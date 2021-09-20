package com.magnificent.monitor.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@Setter
public class Config {

    @Getter
    @Value("${SERVER_URL:http://localhost:8081/}")
    private URI serverURL;

    @Value("${PING_INTERVAL:10}")
    private int pingInterval;

    @Value("${REPORT_INTERVAL:60}")
    private int reportInterval;

    public void setServerURL(String serverURL) {
        this.serverURL = URI.create(serverURL);
    }

    public long pingIntervalInMilliSeconds() {
        return pingInterval * 1000;
    }

    public long reportIntervalInMilliSeconds() {
        return reportInterval * 1000;
    }
}
