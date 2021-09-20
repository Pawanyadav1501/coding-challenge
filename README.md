# Magnificent Monitor, Server

## Prerequisits
Required to build and run without docker:
* `java 11`
* `mvn`

Required to build and run via docker:
* `mvn`
* `docker`
* `docker-compose`

## Installation

#### with Docker magnificent-monitor
```
# build the jar
mvn clean package -f magnificent-monitor/pom.xml
```

```
# build the docker images
docker-compose build
```
```
# get everything up an running
docker-compose up
```



#### Run without Docker magnificent-monitor
```
# build the jar
mvn clean package -f magnificent-monitor/pom.xml
```

#### with Docker magnificent-server
```
# build the jar
mvn clean package -f magnificent-server/pom.xml
```

```
# build the docker images
docker-compose build
```
```
# get everything up an running
docker-compose up
```



#### Run without Docker magnificent-server
```
# build the jar
mvn clean package -f magnificent-server/pom.xml
```

```
# run the server
java -jar magnificent-server/target/magnificent-server-0.0.1-SNAPSHOT.jar
```

```
# run the monitor
java -jar magnificent-monitor/target/magnificent-monitor-0.0.1-SNAPSHOT.jar
```

**NOTE:**

By default the monitor expects the magnificent-server to be available at localhost:8081. For more extensive 
configuration management see [the README of the monitor](magnificent-monitor/README.md)
