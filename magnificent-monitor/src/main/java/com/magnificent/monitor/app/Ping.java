package com.magnificent.monitor.app;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDateTime;


/**
 * A Ping represents the result of a GET request to a webserver and holds information whether a request was successful
 *  or failed. Pings are used to determine the health of a webserver.
 */
@Getter
public class Ping {

    private final LocalDateTime responseTime;

    private HttpStatus responseStatus;

    private String destination;


    static Ping constructFor(URI destination, ResponseEntity<String> response) {
        return new Ping(destination.toString(), response.getStatusCode());
    }


    Ping(String destination, HttpStatus responseStatus) {
        // this is just an approximation to the actual response time.
        this.responseTime = LocalDateTime.now();

        this.setDestination(destination);
        this.setResponseStatus(responseStatus);
    }


    boolean wasSuccessful(){
        return responseStatus.equals(HttpStatus.OK);
    }


    boolean indicatesUnresponsiveServer() {
        return this.getResponseStatus().equals(HttpStatus.SERVICE_UNAVAILABLE);
    }


    private void setResponseStatus(HttpStatus responseStatus) {
        if (responseStatus == null){
            throw new RuntimeException("The status code of a Ping may not be null!");
        }

        boolean statusCodeIsAsExpected = responseStatus.equals(HttpStatus.OK) || responseStatus.equals(HttpStatus.SERVICE_UNAVAILABLE) || responseStatus.equals(HttpStatus.INTERNAL_SERVER_ERROR);

        if ( ! statusCodeIsAsExpected ){
            throw new RuntimeException(String.format("The monitor can currently not handle the status code: %s ", responseStatus.toString()));
        }

        this.responseStatus = responseStatus;
    }


    private void setDestination(String destination) {
        if(StringUtils.isEmpty(destination)){
            throw new RuntimeException("The destination of a Ping may not be empty!");
        }
        this.destination = destination;
    }
}
