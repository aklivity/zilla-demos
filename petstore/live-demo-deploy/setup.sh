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

echo "NAMESPACE=$NAMESPACE"
echo "KAFKA_BOOTSTRAP=$KAFKA_BOOTSTRAP"
echo "KAFKA_BOOTSTRAP_HOST=$KAFKA_BOOTSTRAP_HOST"
echo "KAFKA_BOOTSTRAP_PORT=$KAFKA_BOOTSTRAP_PORT"
echo "KAFKA_USER=$KAFKA_USER"
echo "KAFKA_PASS=$KAFKA_PASS"

## Installing services

# # Ingress controller
# helm upgrade --install ingress-nginx ingress-nginx -n $NAMESPACE --create-namespace --repo https://kubernetes.github.io/ingress-nginx --wait \
#     --set-string controller.service.annotations."service\.beta\.kubernetes\.io/aws-load-balancer-type"="nlb" \

# Print public ports
# echo "==== $NAMESPACE Ingress controller is serving ports: $(kubectl get svc -n $NAMESPACE ingress-nginx-controller --template "{{ range .spec.ports }}{{.port}} {{ end }}")"

# Zilla Petstore Demo
helm upgrade --install zilla oci://ghcr.io/aklivity/charts/zilla --version 0.9.73 -n $NAMESPACE --create-namespace --wait \
    --set-file configMaps.specs.data.petstore-openapi\\.yaml=petstore-openapi.yaml \
    --set-file configMaps.specs.data.petstore-kafka-asyncapi\\.yaml=petstore-kafka-asyncapi.yaml \
    --set-file zilla\\.yaml=zilla.yaml \
    --set extraEnv[0].value="$KAFKA_BOOTSTRAP" \
    --set extraEnv[1].value="$KAFKA_USER",extraEnv[2].value="$KAFKA_PASS" \
    --values values.yaml
