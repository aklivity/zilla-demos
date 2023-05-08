#!/bin/bash
set -x

# Stop port forwarding
pgrep kubectl && killall kubectl

# Uninstall Zilla engine
helm uninstall zilla-vortex-demo --namespace zilla-vortex-demo
kubectl delete namespace zilla-vortex-demo
