package com.magnificent.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class MagnificentMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagnificentMonitorApplication.class, args);
	}


	@ConditionalOnProperty(value = "monitor.scheduling.enabled", havingValue = "true")
	@Configuration
	@EnableScheduling
	public static class SchedulingConfiguration {
	}
}
