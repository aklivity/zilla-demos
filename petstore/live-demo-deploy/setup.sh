#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

NAMESPACE="${NAMESPACE:-petstore}"
KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-kafka.zilla-kafka-broker.svc.cluster.local:9092}"
KAFKA_BOOTSTRAP_HOST="${KAFKA_BOOTSTRAP_HOST:-kafka.zilla-kafka-broker.svc.cluster.local}"
KAFKA_BOOTSTRAP_PORT="${KAFKA_BOOTSTRAP_PORT:-9092}"
KAFKA_USER="${KAFKA_USER:-admin}"
KAFKA_PASS="${KAFKA_PASS:-admin}"
SASL_JAAS="org.apache.kafka.common.security.scram.ScramLoginModule required username=\"$KAFKA_USER\" password=\"$KAFKA_PASS\";"

echo "NAMESPACE=$NAMESPACE"
echo "KAFKA_BOOTSTRAP=$KAFKA_BOOTSTRAP"
echo "KAFKA_BOOTSTRAP_HOST=$KAFKA_BOOTSTRAP_HOST"
echo "KAFKA_BOOTSTRAP_PORT=$KAFKA_BOOTSTRAP_PORT"
echo "KAFKA_USER=$KAFKA_USER"
echo "KAFKA_PASS=$KAFKA_PASS"
echo "SASL_JAAS=$SASL_JAAS"

## Installing services

# # Ingress controller
helm upgrade --install ingress-nginx ingress-nginx -n $NAMESPACE --create-namespace --repo https://kubernetes.github.io/ingress-nginx --wait \
    --set-string controller.service.annotations."service\.beta\.kubernetes\.io/aws-load-balancer-type"="nlb" \


# Print public ports
# echo "==== $NAMESPACE Ingress controller is serving ports: $(kubectl get svc -n $NAMESPACE ingress-nginx-controller --template "{{ range .spec.ports }}{{.port}} {{ end }}")"

# Zilla Taxi Demo
helm upgrade --install zilla oci://ghcr.io/aklivity/charts/zilla --version 0.9.72 -n $NAMESPACE --wait \
    --set-file configMaps.specs.data.tracking-openapi\\.yaml=tracking-openapi.yaml \
    --set-file zilla\\.yaml=zilla.yaml \
    --set extraEnv[0].value="$KAFKA_BOOTSTRAP" \
    --set extraEnv[1].value="$KAFKA_USER",extraEnv[2].value="$KAFKA_PASS" \
    --values values.yaml
