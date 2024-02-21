#!/bin/bash
set -e

# Install Zilla to the Kubernetes cluster with helm and wait for the pod to start up
NAMESPACE=grpc-proxy
ZILLA_NAME=zilla
helm upgrade --install $ZILLA_NAME oci://ghcr.io/aklivity/charts/zilla --version 0.9.68 --namespace $NAMESPACE --create-namespace --wait \
    --set-file configMaps.proto.data.route_guide\\.proto=route-guide/route_guide.proto \
    --set-file zilla\\.yaml=./zilla.yaml \
    --values values.yaml

helm upgrade --install route-guide route-guide --namespace $NAMESPACE --wait
helm upgrade --install kafka-ui kafka-ui --version 0.7.5 --namespace $NAMESPACE --repo https://provectus.github.io/kafka-ui-charts --values kafka-ui-values.yaml --wait
helm upgrade --install prometheus prometheus --version 25.13.0 --namespace $NAMESPACE --repo https://prometheus-community.github.io/helm-charts --values prometheus-values.yaml --wait

# Start port forwarding
ZILLA_PORTS=$(kubectl get svc --namespace $NAMESPACE $ZILLA_NAME --template "{{ range .spec.ports }}{{.port}} {{ end }}")
UI_POD_NAME=$(kubectl get pods --namespace $NAMESPACE -l "app.kubernetes.io/name=kafka-ui,app.kubernetes.io/instance=kafka-ui" -o jsonpath="{.items[0].metadata.name}")
echo "Serving Zilla ports $ZILLA_PORTS"
echo "Visit http://127.0.0.1:8080 to use your application"
eval "kubectl port-forward --namespace $NAMESPACE service/$ZILLA_NAME $ZILLA_PORTS & kubectl --namespace $NAMESPACE port-forward $UI_POD_NAME 8080:8080 &"

