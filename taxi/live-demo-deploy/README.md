# Zilla gRPC Proxy on K8s

This demo deploys a gRPC proxy with Zilla to a K8s cluster with a public endpoint. The storage layer is a SASL/SCRAM auth Kafka provider. Metrcis are scrapped and pushed to a public prometheus instance.

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

    ```shell
    ./teardown.sh
    ```



Mar 26, 2024 3:21:19 PM org.eclipse.yasson.internal.Marshaller marshall
SEVERE: Problem adapting object of type class io.aklivity.zilla.runtime.engine.config.NamespaceConfig to interface jakarta.json.JsonObject in class class io.aklivity.zilla.runtime.engine.internal.config.NamespaceAdapter
Mar 26, 2024 3:21:20 PM org.eclipse.yasson.internal.Marshaller marshall
SEVERE: Generated JSON is not completed.
Problem adapting object of type class io.aklivity.zilla.runtime.engine.config.NamespaceConfig to interface jakarta.json.JsonObject in class class io.aklivity.zilla.runtime.engine.internal.config.NamespaceAdapter
error
java.util.concurrent.ExecutionException: io.aklivity.zilla.runtime.engine.config.ConfigException: Engine configuration failed
	at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
	at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2073)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.Engine.start(Engine.java:257)
	at io.aklivity.zilla.runtime.command.start@0.9.74/io.aklivity.zilla.runtime.command.start.internal.airline.ZillaStartCommand.run(ZillaStartCommand.java:164)
	at io.aklivity.zilla.runtime.command@0.9.74/io.aklivity.zilla.runtime.command.internal.ZillaMain$Invoker.invoke(ZillaMain.java:69)
	at io.aklivity.zilla.runtime.command@0.9.74/io.aklivity.zilla.runtime.command.internal.ZillaMain.invoke(ZillaMain.java:40)
	at io.aklivity.zilla.runtime.command@0.9.74/io.aklivity.zilla.runtime.command.internal.ZillaMain.main(ZillaMain.java:34)
Caused by: io.aklivity.zilla.runtime.engine.config.ConfigException: Engine configuration failed
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.registry.EngineManager.reconfigure(EngineManager.java:163)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.registry.FileWatcherTask.watch(FileWatcherTask.java:118)
	... 5 more
Caused by: jakarta.json.bind.JsonbException: Problem adapting object of type class io.aklivity.zilla.runtime.engine.config.NamespaceConfig to interface jakarta.json.JsonObject in class class io.aklivity.zilla.runtime.engine.internal.config.NamespaceAdapter
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.serializer.AdaptedObjectSerializer.serialize(AdaptedObjectSerializer.java:73)
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.Marshaller.serializeRoot(Marshaller.java:147)
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.Marshaller.marshall(Marshaller.java:73)
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.Marshaller.marshall(Marshaller.java:101)
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.JsonBinding.toJson(JsonBinding.java:134)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.config.EngineConfigWriter.write0(EngineConfigWriter.java:187)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.config.EngineConfigWriter.write0(EngineConfigWriter.java:160)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.config.EngineConfigWriter.write(EngineConfigWriter.java:91)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.registry.EngineManager.lambda$parse$5(EngineManager.java:210)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
	at java.base/java.util.LinkedList$LLSpliterator.forEachRemaining(LinkedList.java:1249)
	at java.base/java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:762)
	at java.base/java.util.stream.ReferencePipeline$7$1.accept(ReferencePipeline.java:276)
	at java.base/java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:1024)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:151)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:174)
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:596)
	at java.base/java.util.stream.ReferencePipeline$7$1.accept(ReferencePipeline.java:276)
	at java.base/java.util.LinkedList$LLSpliterator.forEachRemaining(LinkedList.java:1249)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:151)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:174)
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:596)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.registry.EngineManager.parse(EngineManager.java:210)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.registry.EngineManager.reconfigure(EngineManager.java:132)
	... 6 more
Caused by: java.lang.NullPointerException: value must not be null.
	at org.leadpony.joy.core@2.1.0/org.leadpony.joy.core.Preconditions.requireNonNull(Preconditions.java:27)
	at org.leadpony.joy.core@2.1.0/org.leadpony.joy.core.JsonArrayBuilderImpl.add(JsonArrayBuilderImpl.java:72)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at io.aklivity.zilla.runtime.binding.tls@0.9.74/io.aklivity.zilla.runtime.binding.tls.internal.config.TlsOptionsConfigAdapter.adaptToJson(TlsOptionsConfigAdapter.java:98)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.config.OptionsConfigAdapter.adaptToJson(OptionsConfigAdapter.java:62)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.config.BindingConfigsAdapter.adaptToJson(BindingConfigsAdapter.java:121)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.config.NamespaceAdapter.adaptToJson(NamespaceAdapter.java:78)
	at io.aklivity.zilla.runtime.engine@0.9.74/io.aklivity.zilla.runtime.engine.internal.config.NamespaceAdapter.adaptToJson(NamespaceAdapter.java:40)
	at org.eclipse.yasson@2.0.3/org.eclipse.yasson.internal.serializer.AdaptedObjectSerializer.serialize(AdaptedObjectSerializer.java:62)
	... 36 more
