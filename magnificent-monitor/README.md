# The (not-so) magnificent monitor
> This monitor regularly pings an endpoint of a webserver (**henceforth: the subject of the monitor**) and reports the health of its 
    subject to the console/log.

#### Content
1. The configuration specs
1. Integration of the log
1. Design Goals & Decisions
1. Functionality
1. Known issues
1. Architecture

## 1. Configuration Specs
This spring boot app is entirely configurable through environment variables. The specs are listed below. *If some terms
 are unclear, please refer to section **4. Functionality.***

 name          | description                                                                | default-value  
 ------------- | -------------------------------------------------------------------------  | -------------- 
 LOG_SEVERITY  | Define the LOG-Severity of messages to be logged                           | INFO     
 SUBJECT_URL   | The endpoint of the web-server (subject) whichs health should be monitored | http://localhost:12345/ 
 PING_INTERVAL | The interval in seconds in which the subjects health should be monitored   | 10       
 REPORT_INTERVAL | The interval in seconds in which the app reports its subjects health     | 60                

 *Further configuration of the log (suchas the logfile to write to) can be acchieved though standard spring config*
 

## 2. Integration of the log
This monitor will regularly write the health of its subject to the log, utilizing the Slf4j logger and a custom message, 
    in json format.

The monitor will log the following message with the log-severity **INFO** to indicate the subjects current health.
```
{
    "type":"health-msg"
    "endpoint" : "http://localhost:12345/",
    "successfulPings" : 10, 
    "failedPings" : 0,
    
    # the 'healthiness' of the server, the percentage of successful requests.
    "healthiness" : 100.0
}
```
*The following shows the actual output:*
```
2020-10-29 13:14:53.774  INFO 18628 --- [   scheduling-1] HealthLog      : {"type":"health-msg","endpoint":"http://localhost:12345/","successfulPings":1,"failedPings":0,"healthiness":100.0}
```
 
#### Unresponive subject
If the monitors subject is unresponsive the monitor will log the following message with the log-severity **WARN**
```
{
    "type":"unresponsive-msg"
    "endpoint" : "http://localhost:12345/",
}
```


## 3. Design Goals &  Decisions
The **design goals** of this app where the following:
* simplicity
* reliability & robustness
* generality & extensibility
* showcasing my coding style
* as few external dependencies as possible

Therefore the following **design decisions** were taken:
* no reliance on a database, everything is persisted in-memory using Java utilities.
* reliance on a 'rich' domain model. This is in line with my coding style and would theoretically allow to easily 
    extend the monitors functionality. 
* The monitor is not implemented to just monitor the health of the `magnificent-server`, but rather can be configured to 
    monitor any other server also.
* Since the requirements on the timeframes for the health reports are very vague, they are configurable.
* The message which this monitor logs should be machine readable, for later analysis. They are therefore written in json
    format.
* implementation is done as a spring boot app, to allow for easy testing, reliability & scheduling capabilities. The embedded webserver is not started though.

## 4. Functionality
Based on the challenge and the design goals the following functionality was decided upon.
* The monitor will ping its subject (`magnificent-server`) in a configurable interval, to determine its health.
* The healthiness of the subject logged in a configurable interval and carries information of successful & failed pings.
    * A ping is successful if it has a 200 HTTP response code.
    * A ping is failed if it has a 500 HTTP response code. 
* The monitor will immediatly report its subject as unresponsive if a ping has a 503 HTTP response code **or** times out.

## 5. Known Issues

1. The monitors persistence is only In-Memory and is therefore not consistent after a restart.
1. The monitor disregards the body of responses to determine its subjects health. Meaning the monitor does not care 
    whether an error message or a `MAGNIFICENT` response was returned. It only inspects the status codes. This seems to 
    be in line with the logic of the magnificent server. This was done in favor of `simplicity`, `generality` and 
    `clarity`. 
1. The monitor only handles the response codes 200, 500 and 503 to determine the health of its subject. 
1. Im no expert in logging formats, I looked into a few standards, but could not find what I was looking for. So the 
   format of the logging output might not be up to standard when it comes to health monitoring, as I´ve came up with a 
   msg format myself.
1. I don´t like that the ServerMonitors methods are directly scheduled. It would be nicer if they would have proper 
    input parameters and a different class would be concerned with scheduling the monitor functionality in the correct 
    configuration.

## 6. Architecture

![The architecture](architecture.jpeg)
