FROM amazoncorretto:21-alpine
COPY ./server/build/libs/cu-all.jar /tmp/server.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar","server.jar"]