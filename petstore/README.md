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

## End User API Migration Video | rough draft

- Start with Pet store OpenAPI spec only includes basic pet crud
- All specs are stored in a registry with appropriate versions for models and specs
- Describe use case to expand api spec scope to include managing Stores and Owners
- Modify both OpenAPI and AsyncAPI specs accordingly in some kind of UI or 3rd party tool
- Publish new versions of specs and models to a registry
- Update Zilla to use new models in a test environment, manual or automated
- Improve the Owner creation API to include some kind of async verification step
- Redeploy changes to test environment
- Demonstrate everything working
- Promote to a production environment
- Verify changes all work

Zilla components
- openapi
- openapi-asyncapi
- asyncapi
- model version updates
- versioning zilla schemas or detecting model version changes
