

helm install \
  cert-manager cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.14.4 \
  --repo https://charts.jetstack.io \
  --set installCRDs=true


kubectl create -f staging-issuer.yaml -n taxi-demo
kubectl create -f prod-issuer.yaml -n taxi-demo
kubectl create -f staging-issuer.yaml -n monitoring
kubectl create -f prod-issuer.yaml -n monitoring
# expected output: issuer.cert-manager.io "letsencrypt-staging" created

kubectl create --edit -f https://raw.githubusercontent.com/cert-manager/website/master/content/docs/tutorials/acme/example/production-issuer.yaml
kubectl create -f zilla-ingress.yaml -n taxi-demo
kubectl create -f zilla-ingress-staging.yaml -n taxi-demo
# expected output: issuer.cert-manager.io "letsencrypt-prod" created


helm upgrade --install ingress-nginx ingress-nginx -n demos-ingress --create-namespace --repo https://kubernetes.github.io/ingress-nginx --wait \
    --set-string controller.service.annotations."service\.beta\.kubernetes\.io/aws-load-balancer-type"="nlb" 
    # --set extraArgs.default-ssl-certificate="taxi-demo/taxi-demo-staging-tls" \
#     --set tcp.7183="taxi-demo/zilla:7183"


#     --set tcp.7114="taxi-demo/zilla:7114" \
#     --set tcp.7151="taxi-demo/zilla:7151"
