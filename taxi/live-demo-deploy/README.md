# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.

```mermaid
flowchart LR

    tmuisr[\Web APP/] -.- |gRPC| zhgs
    tmuisr -.- |HTTP| ztos

    subgraph Zilla Taxi Hailing
            zhgs{{Protobuf Dispatch Service}}
            zhgs --- zhig[consume]
            zhgs --- zheg[produce]
    end

    subgraph Zilla Taxi Tracking
            ztos{{OpenAPI REST}} --- ztoc[consume]
            ztas{{AsyncAPI MQTT}} --- ztapc[pub/sub]
    end
        zhig -.-> |gRPC| tsgrpc[Dispatch Service]
        tsgrpc --> tsiot[Taxi 1]
        tsgrpc --> tsiotb[Taxi 2]
        tsgrpc --> tsiotc[Taxi 3]
        tsiotc -.-> |MQTT| ztas
        tsiotb -.-> |MQTT| ztas
        tsiot -.-> |MQTT| ztas

    subgraph Confluent Cloud
        cciot[[Tracking Kafka Cluster]]
        ztapc -.- cciot
        ztoc -.- cciot
        ccsm[[Hailing Kafka Cluster]]
        zheg -.- ccsm
        zhig -.- ccsm
    end
```

## Installing

- Create a `.env` file or export the below variables.

    ```text
    NAMESPACE=
    KAFKA_BOOTSTRAP=
    KAFKA_USER=
    KAFKA_PASS=
    PROM_PASS=
    ```

- Set your desired k8s cluster config.
- Install all of the services with the setup script.

    ```shell
    ./setup.sh
    ```

- You can use this deployment with the gRPC-Proxy endpoints in the [Zilla Quickstart](https://docs.aklivity.io/zilla/latest/tutorials/quickstart/kafka-proxies.html#postman-collections).
- Uninstall all of the services with the teardown script.

    ```shell
    ./teardown.sh
    ```
