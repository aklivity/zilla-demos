/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import io.aklivity.zilla.demo.streampay.data.model.User;

public class ProcessUserSupplier implements ProcessorSupplier<String, User, String, User>
{
    private String userStoreName;

    public ProcessUserSupplier(
        String userStoreName)
    {
        this.userStoreName = userStoreName;
    }

    @Override
    public Processor<String, User, String, User> get()
    {
        return new UserProcessor();
    }

    class UserProcessor implements Processor<String, User, String, User>
    {
        private ProcessorContext context;
        private KeyValueStore<String, User> userStore;

        @Override
        public void init(
            final ProcessorContext context)
        {
            this.context = context;
            this.userStore = context.getStateStore(userStoreName);
        }

        @Override
        public void process(
            Record<String, User> record)
        {
            userStore.put(record.key(), record.value());
        }
    }
}
