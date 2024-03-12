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


kubectl delete namespace zilla-taxi-hailing
kubectl delete namespace zilla-taxi-tracking

kubectl delete all -n zilla-taxi-hailing --all


helm uninstall ingress-nginx --namespace zilla-taxi-hailing --ignore-not-found
zilla-taxi-hailing
