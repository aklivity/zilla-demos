/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream.processor;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import io.aklivity.zilla.demo.streampay.data.model.Command;

public class ValidateCommandSupplier implements ProcessorSupplier<String, Command, String, Command>
{
    private final String idempotencyKeyStoreName;
    private final String successName;
    private final String failureName;

    public ValidateCommandSupplier(
        String idempotencyKeyStoreName,
        String successName,
        String failureName)
    {
        this.idempotencyKeyStoreName = idempotencyKeyStoreName;
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
        private  KeyValueStore<byte[], byte[]> idempotencyKeyStore;

        @Override
        public void init(
            final ProcessorContext context)
        {
            this.context = context;
            this.idempotencyKeyStore = context.getStateStore(idempotencyKeyStoreName);
        }

        @Override
        public void process(
            Record<String, Command> record)
        {
            final Headers headers = record.headers();
            final Header correlationId = headers.lastHeader("zilla:correlation-id");
            final Header idempotencyKey = headers.lastHeader("idempotency-key");
            final Header userId = headers.lastHeader("zilla:identity");

            final Headers newHeaders = new RecordHeaders();
            newHeaders.add(correlationId);
            if (idempotencyKey != null)
            {
                newHeaders.add(idempotencyKey);
            }
            newHeaders.add(userId);


            final Record<String, Command> command = record.withHeaders(newHeaders);
            final byte[] idempotencyKeyValue = idempotencyKeyStore.get(idempotencyKey.value());
            final String childName = idempotencyKeyValue == null || idempotencyKeyValue != userId.value()
                ? successName : failureName;

            context.forward(command, childName);
            if (childName.equals(successName))
            {
                idempotencyKeyStore.put(idempotencyKey.value(), userId.value());
            }
        }
    }
}
