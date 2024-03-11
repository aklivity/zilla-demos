# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.

```mermaid
flowchart

    tmuisr[\Web UI/] -.- |HTTP| har
    tmuisr -.- |HTTP| ztos
        
    subgraph Taxi Hailing

        subgraph service/hailing-app
            har[Web APP]
        end

        subgraph service/zilla-hailing
            har --- zhgs{{gRPC service}}
            zhgs --- zhp[pods]
            zhp --- zheg{{egress CC}}
            zhp --- zhig{{ingress CC}}
        end
    end

    subgraph Taxi Tracking
        subgraph service/zilla-tracking
            ztos{{OpenAPI REST}}
            ztos --- ztp[pods]
            ztas{{AsyncAPI MQTT}} --- ztp[pod]
            ztp[pods] --- zteg{{egress CC}}
        end
        subgraph service/taxi-tracking
            zhgs --> |gRPC| tsgrpc{{Hailing service}}
            tsgrpc --> tsiot[IoT Devices]
            tsiot --> |MQTT| ztas
        end
    end

    subgraph Confluent Cloud
        cck[[Kafka]]
        zteg -.- cck
        zheg -.- cck
        zhig -.- cck
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
