# Vortex

This demo is created to showcase multiple protocols being handled by Zilla with ease and executing a high volume of messages with low latency.

## Architecture

- zilla configure
  - local kafka
  - grpc, rest, SSE to and from Kafka
  - flow
    - ui(http) init
  - loop
    - http to zilla to kafka-json
    - kafka-json to kafka-proto
    - kafka to zilla to grpc to microservice
    - micro to kafka-json
    - kafka-json to zilla to SSE to ui
    - ui to http
  - The user can create a message and let it loop. then the user can initiate a stop command which will trigger a dup of the number of loops.
    - need
      - accurate telemetry for each message
      - write the message journey in the message
  - Leverage a time to live in the message
  - microservice
    - tracking number of loops
    - checking the time to live
  - Leveraging other bindings without hitting Kafka?
- Demo delivery
  - Friday will deliver a single loop
  - MQTT, Metrics?
