/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;

import io.aklivity.zilla.demo.streampay.data.model.User;
import io.aklivity.zilla.demo.streampay.simulation.service.SimulateUser;

public class UserProcessor implements Processor<String, User, String, User>
{
    private final SimulateUser simulateUser;

    public UserProcessor(
        SimulateUser simulateUser)
    {
        this.simulateUser = simulateUser;
    }


    @Override
    public void process(
        Record record)
    {
        final String key = (String) record.key();
        final User user = (User) record.value();

        simulateUser.insertUser(key, user);
    }
}
