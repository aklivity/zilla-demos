# Taxi Demo

This demo was created to showcase the MQTT protocol brokered by Zilla. It uses [Open Street Maps](https://www.openstreetmap.org/), [Open Route Service](https://openrouteservice.org/), and the [MQTT Simulator](https://github.com/DamascenoRafael/mqtt-simulator) to demonstrate a real world taxi hailing and location tracking service.

![zilla-taxi-demo-diagram](.assets/zilla-taxi-demo-diagram@2x.png)

## Requirements

- Docker Compose

## Setup

1. Start all of the services using `docker-compose`. The `startup.sh` script will `build` and `start` all of the needed services. This command will also `restart` an existing stack.

    ```bash
    ./startup.sh
    ```

    > This will take a long time to build the first time it is run since it will need to download maven and npm packages.

1. Open the Open Street Maps UI at [localhost](http://localhost/). The map is centered on the San Jose Convention center.
1. Click somewhere or search for a local destination.
1. Click the directions button for the selected location.
1. The location will be filed as the destination with the San Jose Convention center being the origin.
1. A Taxi marker will appear along the route and travel along it for the duration shown in the popup.

## Using the Demo UI

The UI has a set of bars highlighted in the downtown San Jose, CA area. Users can hail taxis to take them to these locations. There are also a number of shuttle busses running route to the bars as well that will make permanent round trips.

- Clicking on one of the bar markers lets you hail a taxi
- The hailed taxi will "pickup" the passenger at the San Jose, CA Convention center
- The taxi will then proceed to the designated location on the map and stop sending location updates

![demo](.assets/taxi-demo.gif)

## Run through

1. Introduce the taxi-demo that demonstrates multiple taxi clients publishing location updates to Kafka using Zilla as an MQTT broker.
1. Walkthrough architecture slide
   1. Cover all different parts of the setup
      - Use the Diagram to describe the architecture
      - Describe the data flow through Zilla using MQTT protocol
   1. Highlight what to note as running through demo
      - Moving taxi/bus icons
      - Ask for input to select a new route
1. Show the demo explaining what is happening at each step
   1. Map UI Hail cab
   1. Show Kafka UI with the latest message key being destination
   1. Show MQTT Postman, can sub to new topic
      - potential ot update bus icon using postman
   1. Show Grafana, number of connected clients should have increased by 1
   1. Back to Map UI
1. End the demo by binging back up the architecture slide and summarizing outcomes and highlighting Zilla benefits

## Load Testing

The mqtt-simulation service includes a `default_routes.json` file which starts a looping set of routes used in the demo. An additional file `default_routes_load_test.json` is available which leverages the simulators ability to generate multiple topics. 

1. You will see in the json file the config for managing the number of topics to generate by updating the `"RANGE_END"` value:

    ```json
    "TYPE": "multiple",
    "RANGE_START": 1,
    "RANGE_END": 500,
    ```

1. The `taxi-service` in the [docker-compose.yaml](docker-compose.yaml) file mounts the default config. Update the volume mount to map the load_test file.

    ```yaml
    volumes:
        - ./grpc/service/default_routes_load_test.json:/usr/src/app/default_routes.json
    ```

1. Ensure the `DEFAUlT_ROUTES` env var is true so the service will start the sim and the `PRINT_SIM_LOGS` is true so the container will print the simulator output.

    ```yaml
    environment:
        DEFAUlT_ROUTES: true
        PRINT_SIM_LOGS: true
    ```

1. Happy Load Testing!


org.agrona.concurrent.AgentTerminationException: java.lang.NumberFormatException: Cannot parse null string
    at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:707)
    at org.agrona.core/org.agrona.concurrent.AgentRunner.doDutyCycle(AgentRunner.java:291)
    at org.agrona.core/org.agrona.concurrent.AgentRunner.run(AgentRunner.java:164)
    at java.base/java.lang.Thread.run(Thread.java:1623)
Caused by: java.lang.NumberFormatException: Cannot parse null string
    at java.base/java.lang.Integer.parseInt(Integer.java:627)
    at java.base/java.lang.Integer.valueOf(Integer.java:992)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onDecodeDescribeResponse(KafkaClientGroupFactory.java:3055)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory.decodeDescribeResponse(KafkaClientGroupFactory.java:789)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.decodeNetwork(KafkaClientGroupFactory.java:2926)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onNetworkData(KafkaClientGroupFactory.java:2562)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onNetwork(KafkaClientGroupFactory.java:2477)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool.doData(KafkaClientConnectionPool.java:308)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientStream.doStreamData(KafkaClientConnectionPool.java:841)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientConnection.onConnectionData(KafkaClientConnectionPool.java:1440)
    at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientConnection.onConnectionMessage(KafkaClientConnectionPool.java:1363)
    at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.handleReadReply(DispatchAgent.java:1244)
    at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.handleRead(DispatchAgent.java:1045)
    at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.concurent.ManyToOneRingBuffer.read(ManyToOneRingBuffer.java:181)
    at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:701)
    ... 3 more
    Suppressed: java.lang.Exception: [engine/data#1]        [0x0101000000000084] streams=[consumeAt=0x0000e788 (0x000000000000e788), produceAt=0x0000e788 (0x000000000000e788)]
            at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:705)
            ... 3 more

 org.agrona.concurrent.AgentTerminationException: java.lang.NumberFormatException: Cannot parse null string
     at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:707)
     at org.agrona.core/org.agrona.concurrent.AgentRunner.doDutyCycle(AgentRunner.java:291)
     at org.agrona.core/org.agrona.concurrent.AgentRunner.run(AgentRunner.java:164)
     at java.base/java.lang.Thread.run(Thread.java:1623)
 Caused by: java.lang.NumberFormatException: Cannot parse null string
     at java.base/java.lang.Integer.parseInt(Integer.java:627)
     at java.base/java.lang.Integer.valueOf(Integer.java:992)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onDecodeDescribeResponse(KafkaClientGroupFactory.java:3055)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory.decodeDescribeResponse(KafkaClientGroupFactory.java:789)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.decodeNetwork(KafkaClientGroupFactory.java:2926)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onNetworkData(KafkaClientGroupFactory.java:2562)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientGroupFactory$DescribeClient.onNetwork(KafkaClientGroupFactory.java:2477)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool.doData(KafkaClientConnectionPool.java:308)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientStream.doStreamData(KafkaClientConnectionPool.java:841)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientConnection.onConnectionData(KafkaClientConnectionPool.java:1440)
     at io.aklivity.zilla.runtime.binding.kafka@0.9.58/io.aklivity.zilla.runtime.binding.kafka.internal.stream.KafkaClientConnectionPool$KafkaClientConnection.onConnectionMessage(KafkaClientConnectionPool.java:1363)
     at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.handleReadReply(DispatchAgent.java:1244)
     at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.handleRead(DispatchAgent.java:1045)
     at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.concurent.ManyToOneRingBuffer.read(ManyToOneRingBuffer.java:181)
     at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:701)
     ... 3 more
     Suppressed: java.lang.Exception: [engine/data#1]        [0x0101000000000064] streams=[consumeAt=0x000095a8 (0x00000000000095a8), produceAt=0x000095a8 (0x00000000000095a8)]
             at io.aklivity.zilla.runtime.engine@0.9.58/io.aklivity.zilla.runtime.engine.internal.registry.DispatchAgent.doWork(DispatchAgent.java:705)
             ... 3 more
