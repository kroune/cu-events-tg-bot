FROM amazoncorretto:21-alpine
COPY ./build/libs/cu-all.jar /tmp/cu-events-bot.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar","cu-events-bot.jar"]