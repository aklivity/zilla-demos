/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.todo.processor;

import java.util.Arrays;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import io.aklivity.zilla.example.todo.model.Command;
import io.aklivity.zilla.example.todo.model.CreateTaskCommand;
import io.aklivity.zilla.example.todo.model.TaskSnapshotState;

public class ValidateCommandSupplier implements ProcessorSupplier<String, Command, String, Command>
{
    private final String taskSnapshotStateStoreName;
    private final String successName;
    private final String failureName;

    public ValidateCommandSupplier(String taskSnapshotStateStoreName, String successName, String failureName)
    {
        this.taskSnapshotStateStoreName = taskSnapshotStateStoreName;
        this.successName = successName;
        this.failureName = failureName;
    }

    @Override
    public Processor<String, Command, String, Command> get()
    {
        return new ValidateCommand();
    }

    class ValidateCommand implements Processor<String, Command, String, Command>
    {
        private ProcessorContext context;
        private KeyValueStore<String, TaskSnapshotState> taskSnapshotStore;

        @Override
        public void init(final ProcessorContext context)
        {
            this.context = context;
            this.taskSnapshotStore = (KeyValueStore) context.getStateStore(taskSnapshotStateStoreName);
        }

        @Override
        public void process(Record<String, Command> record)
        {
            final String key = record.key();
            final Command command = record.value();
            final Headers headers = record.headers();
            final Header correlationId = headers.lastHeader("zilla:correlation-id");
            final Header idempotencyKeyHeader = headers.lastHeader("idempotency-key");
            final Header path = headers.lastHeader(":path");
            final Header ifMatch = headers.lastHeader("if-match");
            final TaskSnapshotState taskSnapshotState = taskSnapshotStore.get(key);
            final String etag = taskSnapshotState == null ? null : taskSnapshotState.getEtag();
            final byte[] idempotencyKey = taskSnapshotState == null ? null : taskSnapshotState.getIdempotencyKey();

            if (checkIdempotencyKey(command, idempotencyKeyHeader, idempotencyKey))
            {
                final Headers newHeaders = new RecordHeaders();
                newHeaders.add(correlationId);
                if (idempotencyKeyHeader != null)
                {
                    newHeaders.add(idempotencyKeyHeader);
                }
                newHeaders.add(path);

                final Record<String, Command> newRecord = record.withHeaders(newHeaders);
                final String childName = ifMatch == null || etag != null && Arrays.equals(ifMatch.value(), etag.getBytes())
                        ? successName : failureName;
                context.forward(newRecord, childName);
            }
        }

        private boolean checkIdempotencyKey(Command command, Header idempotencyKeyHeader, byte[] idempotencyKey)
        {
            return idempotencyKeyHeader == null ||
                    !(command instanceof CreateTaskCommand) ||
                    !Arrays.equals(idempotencyKeyHeader.value(), idempotencyKey);
        }
    }
}
