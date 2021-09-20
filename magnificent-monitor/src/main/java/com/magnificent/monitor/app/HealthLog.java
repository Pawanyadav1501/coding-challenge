package com.magnificent.monitor.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility which reports the current health of a server based and allows for modification and testability of logging.
 */
@Slf4j
@Component
public class HealthLog {

    private final ObjectMapper json = new ObjectMapper();


    public void report(Health health){

        if(health == null){
            throw new RuntimeException("health to report may not be null.");
        }

        try{
            log.info(json.writeValueAsString(health));
        }catch (JsonProcessingException e){
            log.error("Could not parse {} to json.", health.toString());
        }
    }


    public void reportUnavailable(Ping ping){

        if(ping == null){
            throw new RuntimeException("ping to report may not be null.");
        }

        if( ! ping.getResponseStatus().equals(HttpStatus.SERVICE_UNAVAILABLE)){
            throw new RuntimeException("a server may only be reported as unavailable if the ping indicates the same.");
        }

        var unresponsiveError = constructUnresponsiveError(ping);

        try{
            log.warn(json.writeValueAsString(unresponsiveError));
        }catch (JsonProcessingException e){
            log.error("Could not parse {} to json.", unresponsiveError.toString());
        }
    }

    private Map<String, Object> constructUnresponsiveError(Ping ping) {
        var message = new HashMap<String, Object>();

        message.put("type", "unresponsive-msg");
        message.put("endpoint", ping.getDestination());
        message.put("time", ping.getResponseTime().format(DateTimeFormatter.ISO_DATE_TIME));

        return message;
    }
}
