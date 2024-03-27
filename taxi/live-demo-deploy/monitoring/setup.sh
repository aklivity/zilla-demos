#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

NAMESPACE="${NAMESPACE:-monitoring}"
PROM_USER="${PROM_USER:-1234}"
PROM_PASS="${PROM_PASS:-admin}"
GRAFANA_PASS="${GRAFANA_PASS:-admin}"
LOKI_URL="${LOKI_URL:-localhost}"

# kubectl create namespace $NAMESPACE
# kubectl create secret generic remotewrite-secret -n $NAMESPACE \
#     --from-literal=username=$PROM_USER \
#     --from-literal=password="$PROM_PASS"

# Prometheus stack metrics collector
# helm upgrade --install prometheus-stack kube-prometheus-stack --version 57.0.3 -n $NAMESPACE --create-namespace --repo https://prometheus-community.github.io/helm-charts \
#     --values prometheus-stack-values.yaml \
#     --set-string grafana.adminPassword="$GRAFANA_PASS" \
helm upgrade --install loki loki-stack -n $NAMESPACE --repo https://grafana.github.io/helm-charts \
    --values loki-values.yaml \
    --set-string promtail.config.clients[0].url="$LOKI_URL"
