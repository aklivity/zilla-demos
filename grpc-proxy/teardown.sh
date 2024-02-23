#!/bin/bash
set -e

# Uninstall services and namespace
NAMESPACE=grpc-proxy
helm uninstall zilla route-guide kafka-ui prometheus ingress-nginx --namespace $NAMESPACE --ignore-not-found
kubectl delete namespace $NAMESPACE
