#!/bin/bash
set -e

NAMESPACE="${NAMESPACE:-petstore}"

## Installing services

# Zilla Petstore Demo
helm upgrade --install zilla oci://ghcr.io/aklivity/charts/zilla --version 0.9.74 -n $NAMESPACE --create-namespace --wait \
    --set-file zilla\\.yaml=zilla.yaml \
    --values values.yaml
