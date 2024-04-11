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
    --payload "scope=read:all write:all" \
    --secret @private.pem \
    | pbcopy
```

## Teardown the environment

```bash
./teardown.sh
```

docker compose down && docker compose up -d
docker compose down zilla && docker compose up zilla -d

curl -X POST -H "Content-Type: application/json;" \
    -H "X-Registry-ArtifactId: petstore-openapi" -H "X-Registry-Version: v2" \
    --data-binary "@petstore-openapi-v2.yaml" http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-openapi/versions
curl -X POST -H "Content-Type: application/json;" \
    -H "X-Registry-ArtifactId: petstore-asyncapi" -H "X-Registry-Version: v2" \
    --data-binary "@petstore-kafka-asyncapi-v2.yaml" http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-asyncapi/versions;
