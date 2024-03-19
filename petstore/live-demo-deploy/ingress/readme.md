https://cert-manager.io/docs/tutorials/acme/nginx-ingress/#step-5---deploy-cert-manager


kubectl get secrets grafana-prod-tls -n monitoring -o json \
 | jq 'del(.metadata["namespace","creationTimestamp","resourceVersion","selfLink","uid","annotations"])' \
 | kubectl apply -n petstore -f -
