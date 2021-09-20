package com.magnificent.monitor.app;

import com.magnificent.monitor.app.Ping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;

/**
 * Utility which executes pings (HTTP GET) to a desired URL, handles errors and constructs a {@link Ping}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class PingUtility {

    private final RestTemplate template;

    Ping ping(URI destination){

        log.debug("executing ping to {}", destination.toString());

        try{
            var response = template.exchange(destination, HttpMethod.GET, null, String.class);
            log.debug("executed ping. received status {} and body {}", response.getStatusCode(), response.getBody());
            return Ping.constructFor(destination, response);
        }catch(ResourceAccessException e){
            log.debug("executed ping. Did not receive a response because {}", e.getMessage());
            return new Ping(destination.toString(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    @Configuration
    public static class PingConfig{

        /**
         * @return a resttemplate configured to gracefully handle 500 and 503 response codes, since they are expected.
         */
        @Bean
        public RestTemplate restTemplate(){
            return new RestTemplateBuilder()
                    .errorHandler(gracefullyHandleExpectedErrors())
                    .build();
        }

        // TODO: using a simpler http client we could throw this code away and instead handle the errors in this.ping()
        private ResponseErrorHandler gracefullyHandleExpectedErrors() {

            return new DefaultResponseErrorHandler() {

                public boolean hasError(ClientHttpResponse response) throws IOException {

                    if (response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)){
                        return false;
                    }

                    if (response.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE)){
                        return false;
                    }

                    return super.hasError(response);
                }

                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    super.handleError(response);
                }
            };
        }
    }

}
