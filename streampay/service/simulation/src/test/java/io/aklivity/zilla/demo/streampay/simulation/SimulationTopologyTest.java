/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.serde.SerdeFactory;

public class SimulationTopologyTest
{
    private static final String PAYMENT_REQUESTS_TOPIC = "payment-requests";
    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String USERS_TOPIC = "users";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, PaymentRequest> paymentRequestsInTopic;
    private TestOutputTopic<String, Transaction> transactionsOutTopic;

    @BeforeEach
    public void setUp()
    {
        final StreamsBuilder builder = new StreamsBuilder();
        final SimulationTopology stream = new SimulationTopology();
        stream.paymentRequestsTopic = PAYMENT_REQUESTS_TOPIC;
        stream.transactionsTopic = TRANSACTIONS_TOPIC;
        stream.usersTopic = USERS_TOPIC;
        stream.buildPipeline(builder);
        final Topology topology = builder.build();

        final Properties props = new Properties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        testDriver = new TopologyTestDriver(topology, props);

        paymentRequestsInTopic = testDriver.createInputTopic(PAYMENT_REQUESTS_TOPIC,
            new StringSerializer(), new JsonSerializer<>());

        final Serde<Transaction> transactionSerde = SerdeFactory.jsonSerdeFor(Transaction.class, false);
        final Serde<String> stringSerde = Serdes.String();
        transactionsOutTopic = testDriver.createOutputTopic(TRANSACTIONS_TOPIC,
            stringSerde.deserializer(), transactionSerde.deserializer());
    }

    @AfterEach
    public void tearDown()
    {
        testDriver.close();
    }

    @Ignore
    @Test
    public void shouldProcessPaymentRequest()
    {
        paymentRequestsInTopic.pipeInput(new TestRecord<>("virtual-user-1", PaymentRequest.builder()
            .id("virtual-user-1")
            .amount(123)
            .fromUserId("virtual-user-1")
            .toUserId("virtual-user-2")
            .timestamp(Instant.now().toEpochMilli())
            .build()));

        List<KeyValue<String, Transaction>> events = transactionsOutTopic.readKeyValuesToList();
        assertEquals(1, events.size());
    }
}
