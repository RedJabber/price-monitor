FROM bellsoft/liberica-openjdk-alpine:11.0.9-12
EXPOSE 8080
CMD ["./mvnw", "package"]
COPY target/*.jar price-monitor.jar
ENTRYPOINT ["java", "-jar", "/price-monitor.jar"]


