#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

NAMESPACE="${NAMESPACE:-taxi-demo}"
KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-kafka.zilla-kafka-broker.svc.cluster.local:9092}"
KAFKA_BOOTSTRAP_HOST="${KAFKA_BOOTSTRAP_HOST:-kafka.zilla-kafka-broker.svc.cluster.local}"
KAFKA_BOOTSTRAP_PORT="${KAFKA_BOOTSTRAP_PORT:-9092}"
KAFKA_USER="${KAFKA_USER:-admin}"
KAFKA_PASS="${KAFKA_PASS:-admin}"
PROM_PASS="${PROM_PASS:-admin}"
SASL_JAAS="org.apache.kafka.common.security.scram.ScramLoginModule required username=\"$KAFKA_USER\" password=\"$KAFKA_PASS\";"

echo "NAMESPACE=$NAMESPACE"
echo "KAFKA_BOOTSTRAP=$KAFKA_BOOTSTRAP"
echo "KAFKA_BOOTSTRAP_HOST=$KAFKA_BOOTSTRAP_HOST"
echo "KAFKA_BOOTSTRAP_PORT=$KAFKA_BOOTSTRAP_PORT"
echo "KAFKA_USER=$KAFKA_USER"
echo "KAFKA_PASS=$KAFKA_PASS"
echo "SASL_JAAS=$SASL_JAAS"
echo "PROM_PASS=$PROM_PASS"

## Installing services

# Ingress controller
helm upgrade --install ingress-nginx ingress-nginx --namespace $NAMESPACE --create-namespace --repo https://kubernetes.github.io/ingress-nginx --values ingress-nginx-values.yaml

# gRPC route_guide.proto server
helm upgrade --install map-ui ./support-services/map-ui --namespace $NAMESPACE --values map-ui-values.yaml

# Zilla Taxi Demo
helm upgrade --install zilla oci://ghcr.io/aklivity/charts/zilla --version 0.9.60 --namespace $NAMESPACE --wait \
    --set-file configMaps.proto.data.taxi_route\\.proto=taxi_route.proto \
    --set-file zilla\\.yaml=zilla.yaml \
    --set extraEnv[0].value="$KAFKA_BOOTSTRAP" \
    --set extraEnv[1].value="$KAFKA_USER",extraEnv[2].value="$KAFKA_PASS" \
    --set extraEnv[3].value="$KAFKA_BOOTSTRAP_HOST",extraEnv[4].value="\"$KAFKA_BOOTSTRAP_PORT\"" \
    --values values.yaml

# Public UI for Kafka
helm upgrade --install kafka-ui kafka-ui --version 0.7.5 --namespace $NAMESPACE --repo https://provectus.github.io/kafka-ui-charts --values kafka-ui-values.yaml \
    --set yamlApplicationConfig.kafka.clusters[0].name="$NAMESPACE" \
    --set yamlApplicationConfig.kafka.clusters[0].bootstrapServers="$KAFKA_BOOTSTRAP" \
    --set yamlApplicationConfig.kafka.clusters[0].properties.sasl\\.jaas\\.config="$SASL_JAAS"

# Prometheus metrics collector
helm upgrade --install prometheus prometheus --version 25.13.0 --namespace $NAMESPACE --repo https://prometheus-community.github.io/helm-charts --values prometheus-values.yaml \
    --set server.remoteWrite[0].basic_auth.password="$PROM_PASS"

# Start port forwarding
INGRESS_PORTS=$(kubectl get svc --namespace $NAMESPACE ingress-nginx-controller --template "{{ range .spec.ports }}{{.port}} {{ end }}")
echo "Ingress controller is serving ports: $INGRESS_PORTS"
