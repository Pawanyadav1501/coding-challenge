package com.magnificent.monitor.app;

import com.magnificent.monitor.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Monitors the health of a webserver (the subject of the monitor) by pinging it and regularly logging the current
 *  health of it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServerMonitor {

    private final Config config;
    private final PingUtility pingUtility;
    private final PingRepository pings;
    private final HealthLog healthLog;

    /**
     * Attempts to {@link Ping} the subject (webserver) of this monitor at the configured endpoint.
     *  The result of the Ping is persisted for later analysis.
     * @return
     */
    @Scheduled(fixedRateString = "#{config.pingIntervalInMilliSeconds()}")
    public Ping pingServer() {

        log.debug("starting ping...");

        var ping = pingUtility.ping(this.config.getServerURL());

        if ( ping.indicatesUnresponsiveServer() ){
            healthLog.reportUnavailable(ping);
        }

        pings.save(ping);

        log.debug("finished ping.");

        return ping;
    }


    @Scheduled(fixedRateString = "#{config.reportIntervalInMilliSeconds()}")
    public void reportHealthOfServer() {

        var beginningOfCurrentInterval = LocalDateTime.now().minus(Duration.ofMillis(this.config.reportIntervalInMilliSeconds()));

        log.debug("reporting health of server for interval starting at {}", beginningOfCurrentInterval);

        var pingsInCurrentInterval = pings.allPingsAfter(beginningOfCurrentInterval);

        var currentHealth = new Health(this.config.getServerURL().toString(), pingsInCurrentInterval);

        log.debug("health calculated, current health is {}", currentHealth);

        healthLog.report(currentHealth);

        log.debug("reported health of server.");
    }
}
