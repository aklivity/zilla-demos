#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

NAMESPACE="${NAMESPACE:-monitoring}"
PROM_PASS="${PROM_PASS:-admin}"

# kubectl create namespace $NAMESPACE
# kubectl create -f pvc.yaml -n $NAMESPACE

# Prometheus stack metrics collector
helm upgrade --install prometheus-stack kube-prometheus-stack --version 57.0.3 -n $NAMESPACE --create-namespace --repo https://prometheus-community.github.io/helm-charts \
    --values prometheus-stack-values.yaml \
    --set spec.remoteWrite[0].basicAuth.password="$PROM_PASS"
helm upgrade --install loki loki-stack -n $NAMESPACE --repo https://grafana.github.io/helm-charts \
    --values loki-values.yaml
