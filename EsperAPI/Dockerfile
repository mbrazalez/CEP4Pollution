FROM openjdk:21-slim

# Expose the specified port to enable communication to/from the container. This is more of a documentation feature and for use by Docker networking.
EXPOSE 8080

# JAVA_OPTS is configured to:
# - Set the heap size to manage memory usage.
# - Use the G1 garbage collector for better performance in heap management.
# - Optimize randomness generation for better startup and runtime performance.
# - Allow access to internal APIs necessary for some Java libraries and frameworks, which may be restricted by default in Java 9+.
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom --add-opens java.base/java.net=ALL-UNNAMED"

# Copy the built application JAR from the host's target directory to the container. 'ADD' is used here for its additional capabilities over 'COPY', though it's simple file copying in this case.
ADD target/API4EventProcessing-0.0.1-SNAPSHOT.jar app.jar

# Set the container's default command to run the application using the defined JAVA_OPTS and ensuring the shell processes environment variable expansions.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
