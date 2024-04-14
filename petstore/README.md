# Zilla Petstore using OpenAPI 3.x & AsyncAPI 3.x

This is an implementation of the common Petstore example where requests are proxied to Kafka. Zilla is implementing the REST endpoints defined in an OpenAPI 3.x spec and proxying them onto Kafka topics defined in an AsyncAPI 3.x spec based on the operations defined in each spec.

## Run the demo locally

The local demo uses a [docker-compose.yaml](docker-compose.yaml) setup and can be started running the [setup.sh](setup.sh) script. Running this script again will only restart the `zilla` service.

This demo implements two different versions of the Petstore API example. You can set the `PETSTORE_VERSION` env var as `v1` or `v2` with the setup script to change between the deployed versions.

|v1 | apicurio registry url|
| -- | -- |
| [petstore-openapi-v1.yaml](petstore-openapi-v1.yaml) | [petstore-openapi/versions/v1](http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-openapi/versions/v1) |
| [petstore-kafka-asyncapi-v1.yaml](petstore-kafka-asyncapi-v1.yaml) | [petstore-asyncapi/versions/v1](http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-asyncapi/versions/v1) |

| v2 | apicurio registry url|
| -- | -- |
| [petstore-openapi-v2.yaml](petstore-openapi-v2.yaml) | [petstore-openapi/versions/v2](http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-openapi/versions/v2) |
| [petstore-kafka-asyncapi-v2.yaml](petstore-kafka-asyncapi-v2.yaml) | [petstore-asyncapi/versions/v2](http://localhost:8081/apis/registry/v2/groups/petstore/artifacts/petstore-asyncapi/versions/v2) |

```bash
PETSTORE_VERSION=v1 ./setup.sh
```

## Generate a REST client

Use your favorite REST client with either OpenAPI spec to generate an interface into this demo. You can also fork our [Public postman collection](https://www.postman.com/aklivity-zilla/workspace/aklivity-zilla-live-demos/collection/28401168-7e92ab31-f56b-4ec7-86df-0774044669a7?action=share&creator=28401168).

## Generate jwt token

Use this script from the root directory of this demo to generate a JWT for authenticating your REST client.

```bash
docker run --rm -v ./private.pem:/private.pem bitnami/jwt-cli encode \
    --alg "RS256" \
    --kid "example" \
    --iss "https://auth.example.com" \
    --aud "https://api.example.com" \
    --exp=+7d \
    --no-iat \
    --payload "scope=read:all write:all write:pets read:pets" \
    --secret @private.pem \
    | pbcopy
```

## Teardown the environment

```bash
./teardown.sh
```
