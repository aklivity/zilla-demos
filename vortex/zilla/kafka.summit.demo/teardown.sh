#!/bin/bash
set -x

# Stop port forwarding
pgrep kubectl && killall kubectl

# Uninstall Zilla engine
helm uninstall zilla-kafka-summit-demo --namespace zilla-kafka-summit-demo
kubectl delete namespace zilla-kafka-summit-demo
