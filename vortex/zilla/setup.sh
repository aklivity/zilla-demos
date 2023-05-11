#!/bin/bash
set -ex

# Install Zilla and Kafka to the Kubernetes cluster with helm and wait for the pods to start up
helm install zilla-vortex-demo chart --namespace zilla-vortex-demo --create-namespace --wait

# Create the required topics in Kafka
KAFKA_POD=$(kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-vortex-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic http_messages \
        --if-not-exists

KAFKA_POD=$(kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-vortex-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic grpc_messages \
        --if-not-exists

KAFKA_POD=$(kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-vortex-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic grpc_exchanges \
        --if-not-exists

KAFKA_POD=$(kubectl get pods --namespace zilla-vortex-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-vortex-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic sse_messages \
        --if-not-exists

# Start port forwarding
kubectl port-forward --namespace zilla-vortex-demo service/zilla 8080 9090 > /tmp/kubectl-zilla.log 2>&1 &
kubectl port-forward --namespace zilla-vortex-demo service/kafka 9092 29092 > /tmp/kubectl-kafka.log 2>&1 &
until nc -z localhost 8080; do sleep 1; done
until nc -z localhost 9090; do sleep 1; done
until nc -z localhost 9092; do sleep 1; done
