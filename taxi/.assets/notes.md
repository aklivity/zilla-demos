aws eks update-kubeconfig --name zilla-demos


k config use-context docker-desktop

k logs -f -l app.kubernetes.io/instance=dispatch-service --all-containers -n taxi-demo
k logs -f -l app.kubernetes.io/instance=zilla --all-containers -n taxi-demo
k logs -f -l app.kubernetes.io/instance=web-app --all-containers -n taxi-demo
k logs -f -l app.kubernetes.io/managed-by=Helm --all-containers -n taxi-demo --max-log-requests=10


- might need for public protection
kubectl apply -f https://raw.githubusercontent.com/nginxinc/kubernetes-ingress/v3.4.3/deploy/crds.yaml

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

## Improvements

- don't validate on fetch
- metrics in multi namespace zilla files

helm:

- zilla.yaml checksum
- zilla.yaml configmap name
- env var map extraEnvMap
- ingress/serviceAccount option
- dedicated local file configmap options
- remove `$` in logs

mqtt:

- don't disconnect client when subscribe address is wrong
- generate uses `*` as wildcard instead of `+`
- generate adds trailing `0`s, perhaps don't add trailing number unless greater than `0`

## Todo

- grafana dashboard
- add more complicated version of diagram
  - kafka rest proxy
  - rest browser backend
  - mqtt broker
  - kafka connect sync and source
- same openapi picture for petstore
- DNS
- London bars
- record example demo walkthrough
- add resource links to the map ui
- review and update documentation





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
