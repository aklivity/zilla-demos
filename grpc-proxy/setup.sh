#!/bin/bash
set -e

# Install Zilla to the Kubernetes cluster with helm and wait for the pod to start up
NAMESPACE=grpc-proxy
helm upgrade --install zilla oci://ghcr.io/aklivity/charts/zilla --version 0.9.68 --namespace $NAMESPACE --create-namespace --wait \
    --set-file configMaps.proto.data.route_guide\\.proto=route-guide/route_guide.proto \
    --set-file zilla\\.yaml=./zilla.yaml \
    --values values.yaml

helm upgrade --install route-guide route-guide --namespace $NAMESPACE --wait
helm upgrade --install kafka-ui kafka-ui --version 0.7.5 --namespace $NAMESPACE --repo https://provectus.github.io/kafka-ui-charts --values kafka-ui-values.yaml --wait
helm upgrade --install prometheus prometheus --version 25.13.0 --namespace $NAMESPACE --repo https://prometheus-community.github.io/helm-charts --values prometheus-values.yaml --wait
helm upgrade --install ingress-nginx ingress-nginx --namespace $NAMESPACE --repo https://kubernetes.github.io/ingress-nginx --values ingress-nginx-values.yaml --wait

# Start port forwarding
INGRESS_PORTS=$(kubectl get svc --namespace $NAMESPACE ingress-nginx-controller --template "{{ range .spec.ports }}{{.port}} {{ end }}")
echo "Ingress controller is serving ports: $INGRESS_PORTS"
