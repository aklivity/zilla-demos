# Zilla Vortex Demo

Zilla example demonstrating HTTP(REST), gRPC & SSE protocol interacting with Kafka Cluster.

![](.assets/zilla.config.svg)

### Requirements

- bash, jq, nc, grpcurl
- Kubernetes (e.g. Docker Desktop with Kubernetes enabled)
- kubectl
- helm 3.0+
- sse-cat
- kcat

### Setup

The `setup.sh` script:

- installs Zilla and Kafka to the Kubernetes cluster with helm and waits for the pods to start up
- creates the `http_messages`, `grpc_messages`, `grpc_exchanges` & `sse_messages` topics in Kafka.
- starts port forwarding

```bash
./setup.sh
```

```text
NAME: zilla-vortex-demo
LAST DEPLOYED: Thu May 11 17:18:50 2023
NAMESPACE: zilla-vortex-demo
STATUS: deployed
REVISION: 1
TEST SUITE: None
...
Connection to localhost port 8080 [tcp/http-alt] succeeded!
Connection to localhost port 9090 [tcp/websm] succeeded!
Connection to localhost port 9092 [tcp/XmlIpcRegSvc] succeeded!
```

### gRPC Client

Build & start gRPC Client & Kafka Client in separate terminal windows.

```bash
cd grpc-client
./mvnw clean install
java -jar target/grpc-client-1.0-SNAPSHOT-shaded.jar
```

```text
...
gRPC Client Started!
```

```bash
cd kafka-event-translator
./mvnw clean install
java -jar target/kafka-event-translator-1.0-SNAPSHOT-shaded.jar
```

```text
...
Kafka Translator Started!
```

### Verify behavior

Connect `sse-cat` client, to verify the events produced by the gRPC Client.
Note that the `events` will not arrive until after `POST` request is triggered in the next step.

```bash
sse-cat http://localhost:8080/sse_messages
```

Send a POST request with an event body.

```bash
curl -v \
       -X "POST" http://localhost:8080/events \
       -H "Content-Type: application/json" \
       -d "{\"greeting\":\"Hello, world\"}"
```

```text
...
> POST /events HTTP/1.1
> Content-Type: application/json
...
< HTTP/1.1 204 No Content
```

Verify that the event has been produced to the `http_messages` Kafka topic.

```bash
kcat -C -b localhost:9092 -t http_messages -J -u | jq .
```

```json
{
  "topic": "http_messages",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1683806019162,
  "broker": 1,
  "headers": [
    "content-type",
    "application/json"
  ],
  "key": null,
  "payload": "{\"greeting\":\"Hello, world\"}"
}
% Reached end of topic http_messages [0] at offset 1
```

Verify message processed(converted to proto format) by Kafka Event Translator Client.

```text
Consumer Record:(null, {"greeting":"Hello, world"}, 0, 0)
```

Verify the JSON payload is converted to proto format and published to `grpc_messages` topic by Kafka Event Translator Client.

```bash
kcat -C -b localhost:9092 -t grpc_messages -J -u | jq .
```

```json
{
  "topic": "grpc_messages",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1683806019345,
  "broker": 1,
  "key": null,
  "payload": "\n\fHello, world"
}
% Reached end of topic grpc_messages [0] at offset 1
```

Event processed by gRPC Client and modified the incoming value.

```text
Found message: message: "Hello, world"
32767: "\001\002\000\002"

```

Verify the message payloads triggered by gRPC Client, followed by a tombstone to mark the end of each request.

```bash
kcat -C -b localhost:9092 -t grpc_exchanges -J -u | jq .
```

```json
{
  "topic": "grpc_exchanges",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1683806019498,
  "broker": 1,
  "headers": [
    "zilla:service",
    "example.DemoService",
    "zilla:method",
    "DemoUnary",
    "zilla:reply-to",
    "grpc_exchanges",
    "zilla:correlation-id",
    "aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d"
  ],
  "key": "aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d",
  "payload": "\n&Hello, world :: been through gRPC Flow"
}
{
  "topic": "grpc_exchanges",
  "partition": 0,
  "offset": 1,
  "tstype": "create",
  "ts": 1683806019502,
  "broker": 1,
  "headers": [
    "zilla:service",
    "example.DemoService",
    "zilla:method",
    "DemoUnary",
    "zilla:reply-to",
    "grpc_exchanges",
    "zilla:correlation-id",
    "aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d"
  ],
  "key": "aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d",
  "payload": null
}
% Reached end of topic grpc_exchanges [0] at offset 2
```

Verify proto format event is converted to text by Kafka Event Translator Client to be consumed by SSE Client.

```text
Consumer Record:(aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d, &Hello, world :: been through gRPC Flow, 0, 0)
```

```bash
kcat -C -b localhost:9092 -t sse_messages -J -u | jq .
```

```json
{
  "topic": "sse_messages",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1683806019533,
  "broker": 1,
  "key": "aba1ef9e-b10c-498d-8b9c-5e281ae9b468-63d4315234e3d00275a85b7a33928b6d",
  "payload": "Hello, world :: been through gRPC Flow"
}
% Reached end of topic sse_messages [0] at offset 1
```

Verify the same using the `sse-cat` client.

```text
sse-cat http://localhost:8080/sse_messages


Hello, world :: been through gRPC Flow

```

### Teardown

The `teardown.sh` script stops port forwarding, uninstalls Zilla and deletes the namespace.

```bash
$ ./teardown.sh
+ pgrep kubectl
6551
6552
+ killall kubectl
+ helm uninstall zilla-vortex-demo --namespace zilla-vortex-demo
release "zilla-vortex-demo" uninstalled
+ kubectl delete namespace zilla-vortex-demo
namespace "zilla-vortex-demo" deleted
```
