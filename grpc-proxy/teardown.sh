#!/bin/bash
set -e

# Uninstall Zilla
NAMESPACE=grpc-proxy
helm uninstall zilla-grpc-proxy route-guide kafka-ui prometheus --namespace $NAMESPACE
