

helm install \
  cert-manager cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.14.4 \
  --repo https://charts.jetstack.io \
  --set installCRDs=true


kubectl create -f prod-issuer.yaml -n taxi-demo
kubectl create -f prod-issuer.yaml -n monitoring
# expected output: issuer.cert-manager.io "letsencrypt-staging" created

kubectl create --edit -f https://raw.githubusercontent.com/cert-manager/website/master/content/docs/tutorials/acme/example/production-issuer.yaml
# expected output: issuer.cert-manager.io "letsencrypt-prod" created