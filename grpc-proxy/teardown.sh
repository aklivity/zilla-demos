#!/bin/bash
set -e

# Uninstall Zilla
NAMESPACE=grpc-proxy
helm uninstall zilla route-guide kafka-ui prometheus ingress-nginx --namespace $NAMESPACE
