FROM openjdk:17-alpine
COPY target/*.jar /app.jar
EXPOSE 5345
ENTRYPOINT ["java","-jar","-XX:+UseSerialGC","/app.jar"]