# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.

```mermaid
flowchart TD

    subgraph Routing Legend
        direction LR
        a -..- |App managed| b
        a ---- |K8s managed| b
    end

    u[\Users/] -->|Public DNS| in(ingress-nginx)

    subgraph k8s
        in --- zsr
        in --- ksr

        subgraph service/zilla
            zsr{{service router}} --- zspa[pod]
            zsr{{service router}} --- zspb[pod]
            zsr{{service router}} --- zspc[pod]
        end
        subgraph service/kafka-ui
            ksr{{service router}} --- kspa[pod]
        end
        subgraph service/route_guide
            rgsr{{service router}} --- rgpa[pod]
            zspa -.- rgsr
            zspb -.- rgsr
            zspc -.- rgsr
        end
        subgraph service/prometheus
            psr{{service router}}---pspa[pod]
        end
    end

    subgraph Confluent Cloud
        cc[[Kafka]] -..- kspa
        cc -..- zspa
        cc -..- zspb
        cc -..- zspc
    end
    subgraph Graphana Cloud
        pspa -.- GC[[Prometheus]]
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
