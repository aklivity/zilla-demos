# Manage and Deploy Open & Async APIs with Zilla

Zilla implements the RESTful APIs described in the OpenAPI Petstore spec and connects to Kafka topics described in the AsyncAPI Petstore spec. Both specs define the allowed operations and security schemas. Zilla easily combines these two API schemas into one REST to Kafka proxy.

## Intro

- petstore open api
- petstore async api
- zilla
- No external dependencies
- zilla is lightweight and uses your existing components

## Steps

- Zilla is a REST proxy
- Implement OpenAPI spec
- Zilla for async workloads
- Implement AsyncAPI spec
- Petstore v2 with async customer verification
- Publish Spec versions to catalog registry
- Zilla redeploy with new model versions
- Client POST async customer request
- Client GET Prefer wait
- Customer verified
- Client GET response
- Promote Zilla to prod cluster
- Switch server context and execute GET

## Recap

With Zilla a Kafka REST proxy has never been easier. Implement and maintain your APIs using the tools and standards you are used to and let Zilla do the rest.
