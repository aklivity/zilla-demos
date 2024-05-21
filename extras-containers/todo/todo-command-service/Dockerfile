FROM openjdk:17-jdk-slim as builder
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 ./mvnw install -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=builder target/todo-service.jar /
ENTRYPOINT ["java", "-jar", "/todo-service.jar"]
