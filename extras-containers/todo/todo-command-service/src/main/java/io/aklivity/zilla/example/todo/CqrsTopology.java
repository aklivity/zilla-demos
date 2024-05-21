/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.todo;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.example.todo.model.Task;
import io.aklivity.zilla.example.todo.model.TaskSnapshotState;
import io.aklivity.zilla.example.todo.processor.ProcessValidCommandSupplier;
import io.aklivity.zilla.example.todo.processor.RejectInvalidCommandSupplier;
import io.aklivity.zilla.example.todo.processor.ValidateCommandSupplier;
import io.aklivity.zilla.example.todo.serde.CommandJsonDeserializer;
import io.aklivity.zilla.example.todo.serde.SerdeFactory;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CqrsTopology
{
    private final Serde<String> stringSerde = Serdes.String();

    private CommandJsonDeserializer commandDeserializer = new CommandJsonDeserializer();
    private final Serde<TaskSnapshotState> taskSnapshotStateSerde =
            SerdeFactory.jsonSerdeFor(TaskSnapshotState.class, false);
    private final Serde<Task> taskSerde = SerdeFactory.jsonSerdeFor(Task.class, false);
    private final Serde<String> responseSerde = Serdes.String();

    @Value("${task.commands.topic}")
    String taskCommandsTopic;

    @Value("${task.snapshots.topic}")
    String taskSnapshotsTopic;

    @Value("${task.replies.topic}")
    String taskRepliesTopic;


    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder)
    {
        final String taskSnapshotStateStore = "TaskSnapshotStateStore";
        final String taskCommandsSource = "TaskCommandsSource";
        final String validateCommand = "ValidateCommand";
        final String taskRepliesSink = "TaskRepliesSink";
        final String rejectInvalidCommand = "RejectInvalidCommand";
        final String processValidCommand = "ProcessValidCommand";
        final String taskSnapshotsSink = "TaskSnapshotsSink";

        // create store
        final StoreBuilder commandStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(taskSnapshotStateStore),
                Serdes.String(),
                taskSnapshotStateSerde);

        Topology topologyBuilder = streamsBuilder.build();
        topologyBuilder.addSource(taskCommandsSource, stringSerde.deserializer(), commandDeserializer, taskCommandsTopic)
                .addProcessor(validateCommand, new ValidateCommandSupplier(taskSnapshotStateStore,
                        processValidCommand, rejectInvalidCommand), taskCommandsSource)
                .addProcessor(processValidCommand,
                        new ProcessValidCommandSupplier(taskSnapshotStateStore, taskSnapshotsSink, taskRepliesSink),
                        validateCommand)
                .addProcessor(rejectInvalidCommand, new RejectInvalidCommandSupplier(taskRepliesSink),
                        validateCommand)
                .addSink(taskRepliesSink, taskRepliesTopic, stringSerde.serializer(), responseSerde.serializer(),
                        processValidCommand, rejectInvalidCommand)
                .addSink(taskSnapshotsSink, taskSnapshotsTopic, stringSerde.serializer(), taskSerde.serializer(),
                        processValidCommand)
                .addStateStore(commandStoreBuilder, validateCommand, processValidCommand);
        System.out.println(topologyBuilder.describe());
    }
}
