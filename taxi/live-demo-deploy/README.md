# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.

```mermaid
flowchart LR

    tmuisr[\Web UI/] -.- |HTTP| har
    tmuisr -.- |HTTP| ztos
        
    subgraph Taxi Hailing

        subgraph hailing-app
            har[Web APP]
        end

        subgraph zilla-dispatch
            har --- |gRPC| zhgs{{gRPC service}}
            zhgs --- zhig[consume]
            zhgs --- zheg[produce]
        end
    end

    subgraph Taxi Tracking
        subgraph zilla-tracking
            ztos{{OpenAPI REST}} --- ztoc[consume]
            ztas{{AsyncAPI MQTT}} --- ztapc[pub/sub]
        end
        zhig -.-> |gRPC| tsgrpc[Dispatch Service]
        tsgrpc --> tsiot[Taxi]
        tsgrpc --> tsiotb[Taxi]
        tsgrpc --> tsiotc[Taxi]
        tsiot --> |MQTT| ztas
        tsiotb --> |MQTT| ztas
        tsiotc --> |MQTT| ztas
    end

    subgraph Confluent Cloud
        cciot[[IOT Kafka Cluster]]
        ztapc -.- cciot
        ztoc -.- cciot
        ccsm[[gRPC Service Mesh Kafka Cluster]]
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
