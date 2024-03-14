# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.



2024-03-12 18:32:47 [client] mqtt-sessions[0] PRODUCE aborted (stopped
2024-03-12 18:32:47 org.agrona.concurrent.AgentTerminationException: java.util.IllegalFormatConversionException: d != io.aklivity.zilla.runtime.engine.internal.stream.Target$$Lambda/0x0000000100504f18
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:766)
2024-03-12 18:32:47     at org.agrona.core/org.agrona.concurrent.AgentRunner.doDutyCycle(AgentRunner.java:291)
2024-03-12 18:32:47     at org.agrona.core/org.agrona.concurrent.AgentRunner.run(AgentRunner.java:164)
2024-03-12 18:32:47     at java.base/java.lang.Thread.run(Thread.java:1583)
2024-03-12 18:32:47 Caused by: java.util.IllegalFormatConversionException: d != io.aklivity.zilla.runtime.engine.internal.stream.Target$$Lambda/0x0000000100504f18
2024-03-12 18:32:47     at java.base/java.util.Formatter$FormatSpecifier.failConversion(Formatter.java:4515)
2024-03-12 18:32:47     at java.base/java.util.Formatter$FormatSpecifier.printInteger(Formatter.java:3066)
2024-03-12 18:32:47     at java.base/java.util.Formatter$FormatSpecifier.print(Formatter.java:3021)
2024-03-12 18:32:47     at java.base/java.util.Formatter.format(Formatter.java:2791)
2024-03-12 18:32:47     at java.base/java.io.PrintStream.implFormat(PrintStream.java:1367)
2024-03-12 18:32:47     at java.base/java.io.PrintStream.format(PrintStream.java:1346)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.binding.kafka@0.9.70/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientProduceFactory$KafkaProduceStream$KafkaProduceClient.onNetworkAbort(KafkaClientProduceFactory.java:1420)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.binding.kafka@0.9.70/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientProduceFactory$KafkaProduceStream$KafkaProduceClient.onNetwork(KafkaClientProduceFactory.java:1313)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleReadReply(EngineWorker.java:1314)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleRead(EngineWorker.java:1108)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.concurent.ManyToOneRingBuffer.read(ManyToOneRingBuffer.java:181)
2024-03-12 18:32:47     at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:760)
2024-03-12 18:32:47     ... 3 more
2024-03-12 18:32:47     Suppressed: java.lang.Exception: [engine/data#3]        [0x03030000000001fc] streams=[consumeAt=0x001bdc38 (0x00000000001bdc38), produceAt=0x001bdc38 (0x00000000001bdc38)]
2024-03-12 18:32:47             at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:764)
2024-03-12 18:32:47             ... 3 more


org.agrona.concurrent.AgentTerminationException: java.lang.NullPointerException: Cannot invoke "io.aklivity.zilla.runtime.binding.kafka.internal.types.KafkaEvaluation.ordinal()" because "evaluation" is null
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:766)
    at org.agrona.core/org.agrona.concurrent.AgentRunner.doDutyCycle(AgentRunner.java:291)
    at org.agrona.core/org.agrona.concurrent.AgentRunner.run(AgentRunner.java:164)
    at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.NullPointerException: Cannot invoke "io.aklivity.zilla.runtime.binding.kafka.internal.types.KafkaEvaluation.ordinal()" because "evaluation" is null
    at io.aklivity.zilla.runtime.binding.kafka@0.9.70/io.aklivity.zilla.runtime.binding.kafka.internal.cache.KafkaCacheCursorFactory.asCondition(KafkaCacheCursorFactory.java:1143)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.70/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaCacheClientFetchFactory.newStream(KafkaCacheClientFetchFactory.java:242)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.70/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaCacheClientFactory.newStream(KafkaCacheClientFactory.java:149)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleBeginInitial(EngineWorker.java:1449)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleDefaultReadInitial(EngineWorker.java:1217)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleReadInitial(EngineWorker.java:1157)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.handleRead(EngineWorker.java:1104)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.concurent.ManyToOneRingBuffer.read(ManyToOneRingBuffer.java:181)
    at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:760)
    ... 3 more
    Suppressed: java.lang.Exception: [engine/data#3]        [0x030300000000023b] streams=[consumeAt=0x0012fab0 (0x000000000212fab0), produceAt=0x0012fc80 (0x000000000212fc80)]
            at io.aklivity.zilla.runtime.engine@0.9.70/io.aklivity.zilla.runtime.engine.internal.registry.EngineWorker.doWork(EngineWorker.java:764)
            ... 3 more

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
