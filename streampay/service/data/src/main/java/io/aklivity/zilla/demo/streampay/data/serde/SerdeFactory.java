/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.data.serde;

import java.util.Map;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;

public final class SerdeFactory
{
    private static CommandSerde commandSerde;

    private SerdeFactory()
    {
    }

    public static CommandSerde commandSerde()
    {
        commandSerde = commandSerde == null ? new CommandSerde() : commandSerde;
        return commandSerde;
    }

    public static <T> Serde jsonSerdeFor(
        Class<T> clazz,
        boolean isKey)
    {
        Map<String, Class<T>> props = Map.of("json.key.type", clazz, "json.value.type", clazz);
        KafkaJsonSerializer<T> ser = new KafkaJsonSerializer();
        ser.configure(props, isKey);
        KafkaJsonDeserializer<T> de = new KafkaJsonDeserializer();
        de.configure(props, isKey);
        return Serdes.serdeFrom(ser, de);
    }
}
