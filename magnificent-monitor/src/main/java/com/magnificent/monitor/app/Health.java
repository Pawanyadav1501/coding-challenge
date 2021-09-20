package com.magnificent.monitor.app;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * This class represents the health of a server based on a number of pings.
 */
@ToString
@Getter
public class Health {

    private final String type = "health-msg";

    private final String endpoint;
    
    private final long successfulPings;
    
    private final long failedPings;

    // the ratio of failed requests in all of the pings.
    private final double healthiness;
    
    
    public Health(String endpoint, List<Ping> pings) {

        this.endpoint = endpoint;

        this.successfulPings = countSuccessfulPingsIn(pings);

        this.failedPings = pings.size() - this.successfulPings;

        this.healthiness = calculateRatioBetween(this.successfulPings, pings.size());
    }
    
    
    private long countSuccessfulPingsIn(List<Ping> pings) {
        return pings.stream()
                .filter(Ping::wasSuccessful)
                .count();
    }

    
    private double calculateRatioBetween(long thePart, long theWhole) {

        if( theWhole == 0 ){
            return 0;
        }

        return ((double) thePart / theWhole ) * 100;
    }
}
