#!/bin/bash
set -e

# Uninstall Zilla
NAMESPACE=zilla-demos
helm uninstall zilla-grpc-proxy route-guide --namespace $NAMESPACE
