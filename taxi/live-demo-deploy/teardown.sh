#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# Uninstall services and namespace
NAMESPACE="${NAMESPACE:-taxi-demo}"
helm uninstall zilla map-ui kafka-ui prometheus ingress-nginx --namespace $NAMESPACE --ignore-not-found
kubectl delete namespace $NAMESPACE
