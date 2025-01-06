FROM openjdk:21-slim

ARG PORT=8080
EXPOSE ${PORT}

ENV MQTTBROKERIP=192.168.18.31
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom --add-opens java.base/java.net=ALL-UNNAMED"
ADD target/API4EventProcessing-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
