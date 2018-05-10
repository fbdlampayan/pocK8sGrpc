FROM openjdk:8u141-jre
LABEL maintainer="francis.lampayan@nokia.com"

COPY 'target/sampleServer-1.0-SNAPSHOT-jar-with-dependencies.jar' /home/jboss/
COPY 'certs/servercert.pem' /home/jboss/
COPY 'certs/serverkey.pem' /home/jboss/
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/home/jboss/sampleServer-1.0-SNAPSHOT-jar-with-dependencies.jar"]