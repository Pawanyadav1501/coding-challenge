# The Magnificent Server

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
