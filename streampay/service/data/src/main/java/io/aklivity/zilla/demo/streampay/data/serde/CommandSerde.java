/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.data.serde;

import org.apache.kafka.common.serialization.Serdes;

import io.aklivity.zilla.demo.streampay.data.model.Command;
import io.confluent.kafka.serializers.KafkaJsonSerializer;

public class CommandSerde extends Serdes.WrapperSerde<Command>
{
    public CommandSerde()
    {
        super(new KafkaJsonSerializer(), new CommandJsonDeserializer());
    }
}

