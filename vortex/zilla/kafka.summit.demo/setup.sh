#!/bin/bash
set -ex

# Install Zilla and Kafka to the Kubernetes cluster with helm and wait for the pods to start up
helm install zilla-kafka-summit-demo chart --namespace zilla-kafka-summit-demo --create-namespace --wait

# Create the echo-commands topic in Kafka
KAFKA_POD=$(kubectl get pods --namespace zilla-kafka-summit-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-kafka-summit-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic echo-messages \
        --if-not-exists

KAFKA_POD=$(kubectl get pods --namespace zilla-kafka-summit-demo --selector app.kubernetes.io/instance=kafka -o name)
kubectl exec --namespace zilla-kafka-summit-demo "$KAFKA_POD" -- \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic events \
        --if-not-exists
        
# Start port forwarding
kubectl port-forward --namespace zilla-kafka-summit-demo service/zilla 8080 9090 > /tmp/kubectl-zilla.log 2>&1 &
kubectl port-forward --namespace zilla-kafka-summit-demo service/kafka 9092 29092 > /tmp/kubectl-kafka.log 2>&1 &
until nc -z localhost 8080; do sleep 1; done
until nc -z localhost 9090; do sleep 1; done
until nc -z localhost 9092; do sleep 1; done