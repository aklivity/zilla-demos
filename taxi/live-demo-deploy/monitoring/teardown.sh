#!/bin/bash
set -e

## Setting up env
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# Uninstall services and namespace
NAMESPACE="${NAMESPACE:-monitoring}"
# uncomment when resources are seperated in different namespaces
kubectl delete all -n $NAMESPACE --all
kubectl delete namespace $NAMESPACE
