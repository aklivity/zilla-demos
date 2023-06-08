# First stage: complete build environment
FROM maven:3.8.4-jdk-11-slim AS builder

# add pom.xml and source code
ADD ./pom.xml pom.xml
ADD ./src src/

# package jar
RUN mvn clean install

# Second stage: minimal runtime environment
From openjdk:11-jre

RUN apt-get update && apt install -y netcat

# copy jar from the first stage
COPY --from=builder target/grpc-client-1.0-SNAPSHOT-shaded.jar grpc-client-1.0-SNAPSHOT-shaded.jar

CMD ["java", "-jar", "grpc-client-1.0-SNAPSHOT-shaded.jar"]