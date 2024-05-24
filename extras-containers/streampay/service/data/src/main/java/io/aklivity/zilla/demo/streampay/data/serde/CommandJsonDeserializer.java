/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.data.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.aklivity.zilla.demo.streampay.data.model.Command;
import io.aklivity.zilla.demo.streampay.data.model.PayCommand;
import io.aklivity.zilla.demo.streampay.data.model.RequestCommand;
import io.confluent.kafka.serializers.jackson.Jackson;

public class CommandJsonDeserializer implements Deserializer<Command>
{
    private ObjectMapper objectMapper = Jackson.newObjectMapper();

    public CommandJsonDeserializer()
    {
    }

    public Command deserialize(
        String topic,
        Headers headers,
        byte[] data)
    {
        Header domainModelHeader = headers.lastHeader("stream-command:operation");
        String domainModel = new String(domainModelHeader.value());
        JavaType type = null;
        switch (domainModel)
        {
        case "PayCommand":
            type = this.objectMapper.getTypeFactory().constructType(PayCommand.class);
            break;
        case "RequestCommand":
            type = this.objectMapper.getTypeFactory().constructType(RequestCommand.class);
        }

        return this.deserialize(data, type);
    }

    public Command deserialize(
        String s,
        byte[] bytes)
    {
        return null;
    }

    Command deserialize(
        byte[] bytes,
        JavaType type)
    {
        try
        {
            return this.objectMapper.readValue(bytes, type);
        }
        catch (Exception var4)
        {
            throw new SerializationException(var4);
        }
    }
}
