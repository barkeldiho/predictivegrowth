FROM openjdk:17-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar", "-agentlib:jdwp=transport=dt_socket,address=*:9091,suspend=n,server=y", "/app.jar"]
EXPOSE 9090 9091
