https://cert-manager.io/docs/tutorials/acme/nginx-ingress/#step-5---deploy-cert-manager


kubectl get secrets taxi-prod-tls -n taxi-demo -o json \
 | jq 'del(.metadata["namespace","creationTimestamp","resourceVersion","selfLink","uid","annotations"])' \
 | kubectl apply -n monitoring -f -


secret/taxi-prod-tls created