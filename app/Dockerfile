FROM openjdk:11-jre
MAINTAINER Paul van Assen <source@pvanassen.nl

ENTRYPOINT ["java", "-jar", "/usr/share/sensorhub/sensorhub.jar"]

EXPOSE 1234/udp
EXPOSE 8080/tcp

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/sensorhub/sensorhub.jar