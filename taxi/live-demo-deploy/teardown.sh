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
# uncomment when resources are seperated in different namespaces
kubectl delete all -n $NAMESPACE --all
kubectl delete namespace $NAMESPACE

helm uninstall zilla -n $NAMESPACE
# helm uninstall map-ui -n $NAMESPACE
# helm uninstall kafka-ui -n $NAMESPACE
# helm uninstall web-app -n $NAMESPACE
# helm uninstall dispatch-service -n $NAMESPACE
# helm uninstall dispatch-service-busses -n $NAMESPACE
