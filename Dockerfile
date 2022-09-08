FROM openjdk:11
COPY /lib/build/libs/lib-deploy.jar app.jar

# for running on raspberrypis
FROM arm32v7/openjdk
COPY /lib/build/libs/lib-deploy.jar app.jar
