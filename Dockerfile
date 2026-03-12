# Dockerfile for Java 25 Spring Boot Application
# Using Amazon Corretto 25 for Java 25 support

FROM amazoncorretto:25 AS runtime

WORKDIR /app

# Copy the pre-built jar file
# Make sure to run: mvn clean package -Dmaven.test.skip=true before building this image
COPY target/assistant-1.0.0-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimizations for containerized environments with Java 25
# - enable-preview: Enable preview features (StructuredTaskScope)
# - UseContainerSupport: Automatically detect container limits
# - MaxRAMPercentage: Use 75% of available memory
# - Virtual Threads: Enabled by default in Java 21+
# - G1GC: Low latency garbage collector
ENV JAVA_OPTS="--enable-preview \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+TieredCompilation \
               -XX:TieredStopAtLevel=1 \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
