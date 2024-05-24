
FROM openjdk:17-jdk-slim as builder
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY data data
COPY simulation simulation
COPY stream stream

RUN --mount=type=cache,target=/root/.m2 ./mvnw install -DskipTests

FROM openjdk:17-jdk-slim
ARG JAR_NAME="application"
COPY --from=builder ${JAR_NAME}/target/${JAR_NAME}.jar /
ENTRYPOINT ["java", "-jar", "/$JAR_NAME.jar"]
