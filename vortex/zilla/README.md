# Zilla Vortex Demo

Zilla example demonstrating HTTP(REST), gRPC & SSE protocol interacting with Kafka Cluster.

<div align="center">
  </br>
  <img src="./.assets/zilla.config.svg">
</div>

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
- creates the `events` & `echo-messages` topic in Kafka.
- starts port forwarding

```bash
$ ./setup.sh
+ helm install zilla-vortex-demo chart --namespace zilla-vortex-demo --create-namespace --wait
NAME: zilla-vortex-demo
LAST DEPLOYED: Fri May  5 22:11:24 2023
NAMESPACE: zilla-vortex-demo
STATUS: deployed
REVISION: 1
TEST SUITE: None
++ kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name
+ KAFKA_POD=pod/kafka-74675fbb8-77xc6
+ kubectl exec --namespace zilla-vortex-demo pod/kafka-74675fbb8-77xc6 -- /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic echo-messages --if-not-exists
Created topic echo-messages.
++ kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name
+ KAFKA_POD=pod/kafka-74675fbb8-77xc6
+ kubectl exec --namespace zilla-vortex-demo pod/kafka-74675fbb8-77xc6 -- /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic events --if-not-exists
Created topic events.
+ nc -z localhost 8080
+ kubectl port-forward --namespace zilla-vortex-demo service/kafka 9092 29092
+ kubectl port-forward --namespace zilla-vortex-demo service/zilla 8080 9090
+ sleep 1
+ nc -z localhost 8080
Connection to localhost port 8080 [tcp/http-alt] succeeded!
+ nc -z localhost 9090
Connection to localhost port 9090 [tcp/websm] succeeded!
+ nc -z localhost 9092
Connection to localhost port 9092 [tcp/XmlIpcRegSvc] succeeded!
```

### gRPC Client

Build gRPC Client

```bash
cd grpc-client/
./mvnw clean install
cd ..
```

### Verify behavior

Start gRPC Client

```bash
cd grpc-client/
java -jar target/grpc-client-1.0-SNAPSHOT-shaded.jar
cd ..
```
```
...
gRPC Client Started!
```

Connect `sse-cat` client, to verify the events produced from gRPC Client.
Note that the `events` will not arrive until after `POST` request is triggered in the next step.

```bash
sse-cat http://localhost:8080/echo-messages
```

Send a POST request with an event body.
```bash
curl -v \
       -X "POST" http://localhost:8080/events \
       -H "Content-Type: application/json" \
       -d "{\"greeting\":\"Hello, world\"}"
```
```
...
> POST /events HTTP/1.1
> Content-Type: application/json
...
< HTTP/1.1 204 No Content
```

Verify that the event has been produced to the events Kafka topic.
```bash
kcat -C -b localhost:9092 -t events -J -u | jq .
```
```
{
  "topic": "events",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1652465273281,
  "broker": 1001,
  "headers": [
    "content-type",
    "application/json"
  ],
  "payload": "{\"greeting\":\"Hello, world\"}"
}
% Reached end of topic events [0] at offset 1
```

Verify messgae processed by gRPC Client
```
Consumer Record:(null, {"greeting":"Hello, world"}, 0, 0)
```


Verify the message payloads triggered by gRPC Client, followed by a tombstone to mark the end of each request.
```bash
kcat -C -b localhost:9092 -t echo-messages -J -u | jq .
```
```
{
  "topic": "echo-messages",
  "partition": 0,
  "offset": 0,
  "tstype": "create",
  "ts": 1683460209896,
  "broker": 1,
  "headers": [
    "zilla:service",
    "example.DemoService",
    "zilla:method",
    "DemoUnary",
    "zilla:correlation-id",
    "474c3b41-a34d-43e3-b1e0-9cab684c91e9-d41d8cd98f00b204e9800998ecf8427e"
  ],
  "key": "474c3b41-a34d-43e3-b1e0-9cab684c91e9-d41d8cd98f00b204e9800998ecf8427e",
  "payload": "\n\fHello, world"
}
{
  "topic": "echo-messages",
  "partition": 0,
  "offset": 1,
  "tstype": "create",
  "ts": 1683460209900,
  "broker": 1,
  "headers": [
    "zilla:service",
    "example.DemoService",
    "zilla:method",
    "DemoUnary",
    "zilla:correlation-id",
    "474c3b41-a34d-43e3-b1e0-9cab684c91e9-d41d8cd98f00b204e9800998ecf8427e"
  ],
  "key": "474c3b41-a34d-43e3-b1e0-9cab684c91e9-d41d8cd98f00b204e9800998ecf8427e",
  "payload": null
}
% Reached end of topic echo-messages [0] at offset 2
```

Verify the same using the `sse-cat` client
```
sse-cat http://localhost:8080/echo-messages


Hello, world

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
