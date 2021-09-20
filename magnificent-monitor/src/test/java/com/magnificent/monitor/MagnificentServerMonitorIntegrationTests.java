package com.magnificent.monitor;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import java.util.stream.IntStream;

import com.magnificent.monitor.app.*;


/**
 * The following is an integration test of the whole system, with the {@link ServerMonitor} as the entrypoint.
 *
 *  The requests sent by the PingUtility are talking to a {@link MockRestServiceServer}. This is possible because the
 *      underlying Rest template of the PingUtility is bound to the mockServer in the Test configuration.
 *
 * The health-logging is tested by Mocking the HealthLog and capturing its inputs arguments throgh a {@link Captor}, this does
 *      not perfectly validate what is actually beeing logged, but is in my opinion the cleanest way to test the output
 *      that will be logged.
 *
 * Testing the system live in its actual scheduled mode is not done, for the sake of simplicity and because the present
 *  tests should be a close enough approximation of the system in live mode.
 */
@SpringBootTest
@TestPropertySource(properties = {"LOG_SEVERITY:DEBUG", "monitor.scheduling.enabled:false", "SUBJECT_URL:http://localhost:8081/", "REPORT_INTERVAL:10000"})
public class MagnificentServerMonitorIntegrationTests {

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ServerMonitor monitor;

    @Autowired
    private PingRepository pings;

    @MockBean
    private HealthLog healthLog;
    @Captor
    private ArgumentCaptor<Health> logArgumentCaptor;

    @TestConfiguration
    public static class Config{

        @Bean
        public MockRestServiceServer mockRestServiceServer(RestTemplate restTemplate){
            return MockRestServiceServer.bindTo(restTemplate).build();
        }
    }


    @Test
    public void monitorHandlesSuccessfullRequestCorrectly(){

        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8081/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess());

        var ping = monitor.pingServer();

        assertEquals(HttpStatus.OK, ping.getResponseStatus(), "the ping after a successfull reponse was not OK.");

        assertEquals("http://localhost:8081/", ping.getDestination(), "An unexpected destination was written to the ping");

        pings.deleteAll();
    }


    @Test
    public void monitorHandleInternalServerErrorCorrectly(){

        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8081/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withServerError());

        var ping = monitor.pingServer();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ping.getResponseStatus(), "the ping after a 500 response was not 500.");

        assertEquals("http://localhost:8081/", ping.getDestination(), "An unexpected destination was written to the ping");

        pings.deleteAll();
    }


    @Test
    public void monitorLogsUnresponsiveServer(){

        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8081/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        var ping = monitor.pingServer();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ping.getResponseStatus(), "the ping after a 504 response was not 504.");

        assertEquals("http://localhost:8081/", ping.getDestination(), "An unexpected destination was written to the ping");

        Mockito.verify(healthLog, Mockito.times(1)).reportUnavailable(ping);

        pings.deleteAll();
    }

    @Test
    public void healthIsCorrectForFiveFailedAndFiveSuccessfulPings(){

        mockServer.expect(ExpectedCount.times(5), requestTo("http://localhost:8081/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));

        mockServer.expect(ExpectedCount.times(5), requestTo("http://localhost:8081/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // ping the server 10 times
        IntStream.rangeClosed(1, 10)
                .forEach(iteration -> monitor.pingServer());

        monitor.reportHealthOfServer();

        Mockito.verify(healthLog, Mockito.times(1)).report(logArgumentCaptor.capture());

        assertEquals(5, logArgumentCaptor.getValue().getFailedPings(), "failed pings were counted incorrectly.");

        assertEquals(5, logArgumentCaptor.getValue().getSuccessfulPings(), "successful were counted incorrectly.");

        assertEquals(50.0, logArgumentCaptor.getValue().getHealthiness(), "healthiness was calculated incorrectly.");

        pings.deleteAll();
    }


    @Test
    public void emptyHealthIsReportedWhenNoPingsPresentYet(){

        monitor.reportHealthOfServer();

        Mockito.verify(healthLog, Mockito.times(1)).report(logArgumentCaptor.capture());

        assertEquals(0, logArgumentCaptor.getValue().getFailedPings(), "failed pings were counted incorrectly.");

        assertEquals(0, logArgumentCaptor.getValue().getSuccessfulPings(), "successful pings were counted incorrectly.");

        assertEquals(0, logArgumentCaptor.getValue().getHealthiness(), "healthiness was calculated incorrectly.");

        pings.deleteAll();

    }
}
