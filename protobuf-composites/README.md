# `protobuf` Composite bindings

Zilla could implement a `protobuf` composite binding that would implement the service level definitions automatically. Every service method would be configured individually as service operations. This would enable the protobuf service methods to map to kafka-asyncapi operations defining which topics to use.

The examples contain the best approximation of the end result with the given `protobuf` and `asyncapi` files.

## protobuf.proxy

A simple implementation of a gRPC service proxy

## asyncapi.protobuf

An example of how to define streaming messages to an existing gRPC service from Kafka.

## protobuf.asyncapi

### req-res

An example mapping both request and response to kafka

### fanin

An example of (eventually) mapping single direction produce into Kafka

### fanout

An example of mapping single direction fetch from Kafka

## Asyncapi Roadmap

From what I could find it looks like Asyncapi is only going to be supporting protobuf as a payload schema the same way Avro would be supported. It looks like the goal is to be able to first support protobuf syntax in asyncapi schema then add the ability to import `#ref` definitions from other file types.

[OpenAPI, RAML, Avro, GraphQL, Protobuf, and XSD users reuse their schema/type definitions within AsyncAPI](https://github.com/asyncapi/shape-up-process/issues/56)

It is common to find the same data structures defined using multiple schema/type definition languages, i.e., JSON Schema, Avro, Protobuf, GraphQL types, etc. If this outcome is achieved, users should be able to link and/or embed their definitions using any of the following languages: OpenAPI Schema, RAML Data Type, Avro, GraphQL Type, Protobuf, and XSD.

> Does this issue include the goal of exporting the *.yml contract as a `.proto` file?
"No,the idea of this issue is to let users reference `.proto` (and other) files inside an AsyncAPI document. And tools should understand it." [source](https://github.com/asyncapi/shape-up-process/issues/56#issuecomment-1187598773)

<https://github.com/asyncapi/protobuf-schema-parser>
