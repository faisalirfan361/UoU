#######
# Packages the app for deployment.
# This assumes the app has already been assembled and jars are in ./build/libs.
#######

####### Stage: Build/prepare the app
FROM alpine:3.16 as build

WORKDIR /output
RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
COPY build/libs/calendars-*SNAPSHOT.jar calendars.jar

####### Stage: Run the app
FROM eclipse-temurin:17.0.4_8-jre-focal as app

WORKDIR /app
COPY --from=build /output /app

CMD ["java", "-javaagent:dd-java-agent.jar", "-jar", "calendars.jar"]

# DO-LATER: Switch to edistroless eventually, probably, but for now it's much easier to have a
# shell to troubleshoot stuff in k8s. Example code:
# FROM gcr.io/distroless/java17-debian11 as app
# ENV JAVA_TOOL_OPTIONS=-javaagent:/app/dd-java-agent.jar
# CMD ["/app/calendars.jar"]
