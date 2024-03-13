aws eks update-kubeconfig --name zilla-demos

- might need for public protection
kubectl apply -f https://raw.githubusercontent.com/nginxinc/kubernetes-ingress/v3.4.3/deploy/crds.yaml

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

## Improvements

- don't validate on fetch

helm:

- zilla.yaml checksum
- zilla.yaml configmap name
- env var map extraEnvMap
- ingress/serviceAccount option
- dedicated local file configmap options
- remove `$` in logs

mqtt:

- don't disconnect client when subscribe address is wrong
- generate uses `*` as wildcard instead of `+`
- generate adds trailing `0`s, perhaps don't add trailing number unless greater than `0`
