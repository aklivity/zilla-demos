# Zilla Openapi Asyncapi Petstore

This demo demonstrate how to use openapi and asyncapi and map them together

## Setup the environment

```bash
./setup.sh
```

## Generate jwt token

```bash
docker run --rm -v ./private.pem:/private.pem bitnami/jwt-cli encode \
    --alg "RS256" \
    --kid "example" \
    --iss "https://auth.example.com" \
    --aud "https://api.example.com" \
    --exp=+7d \
    --no-iat \
    --payload "scope=read:pets write:pets" \
    --secret @private.pem \
    | pbcopy
```

## Teardown the environment

```bash
./teardown.sh
```
