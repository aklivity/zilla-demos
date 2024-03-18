#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

PROM_PASS="${PROM_PASS:-admin}"

echo "PROM_PASS=$PROM_PASS"

# kubectl create namespace monitoring
# kubectl create -f pvc.yaml -n monitoring

# Prometheus stack metrics collector
helm upgrade --install prometheus-stack kube-prometheus-stack --version 57.0.3 -n monitoring --repo https://prometheus-community.github.io/helm-charts \
     --values prometheus-stack-values.yaml
helm upgrade --install loki loki-stack -n monitoring --repo https://grafana.github.io/helm-charts \
    --values loki-values.yaml
    # --set server.remoteWrite[0].basic_auth.password="$PROM_PASS"
