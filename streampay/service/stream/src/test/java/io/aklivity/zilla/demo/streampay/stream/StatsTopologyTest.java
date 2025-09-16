/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.aklivity.zilla.demo.streampay.data.model.Event;
import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.model.User;
import io.aklivity.zilla.demo.streampay.data.serde.SerdeFactory;

public class StatsTopologyTest
{
    private static final String PAYMENT_REQUESTS_TOPIC = "payment-requests";
    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String ACTIVITIES_TOPIC = "activities";
    private static final String USERS_TOPIC = "users";
    private static final String BALANCES_TOPIC = "balances";
    private static final String BALANCE_HISTORIES_TOPIC = "balance-histories";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, User> usersInTopic;
    private TestInputTopic<String, Transaction> transactionsInTopic;
    private TestInputTopic<String, PaymentRequest> paymentRequestsInTopic;
    private TestOutputTopic<String, Event> eventOutTopic;

    @BeforeEach
    public void setUp()
    {
        final StreamsBuilder builder = new StreamsBuilder();
        final StatsTopology stream = new StatsTopology();
        stream.paymentRequestsTopic = PAYMENT_REQUESTS_TOPIC;
        stream.transactionsTopic = TRANSACTIONS_TOPIC;
        stream.activitiesTopic = ACTIVITIES_TOPIC;
        stream.usersTopic = USERS_TOPIC;
        stream.balancesTopic = BALANCES_TOPIC;
        stream.balanceHistoriesTopic = BALANCE_HISTORIES_TOPIC;
        stream.buildPipeline(builder);
        final Topology topology = builder.build();

        final Properties props = new Properties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        testDriver = new TopologyTestDriver(topology, props);

        usersInTopic = testDriver.createInputTopic(USERS_TOPIC,
            new StringSerializer(), new JsonSerializer<>());

        transactionsInTopic = testDriver.createInputTopic(TRANSACTIONS_TOPIC,
                new StringSerializer(), new JsonSerializer<>());

        paymentRequestsInTopic = testDriver.createInputTopic(PAYMENT_REQUESTS_TOPIC,
            new StringSerializer(), new JsonSerializer<>());

        final Serde<Event> eventSerde = SerdeFactory.jsonSerdeFor(Event.class, false);
        final Serde<String> stringSerde = Serdes.String();
        eventOutTopic = testDriver.createOutputTopic(ACTIVITIES_TOPIC,
            stringSerde.deserializer(), eventSerde.deserializer());
    }

    @AfterEach
    public void tearDown()
    {
        testDriver.close();
    }

    @Test
    public void shouldProcessTransaction()
    {
        usersInTopic.pipeInput(new TestRecord<>("alice", User.builder()
            .id("alice")
            .name("Alice")
            .username("alice")
            .build()));
        usersInTopic.pipeInput(new TestRecord<>("bob", User.builder()
            .id("bob")
            .name("Bob")
            .username("bob")
            .build()));

        transactionsInTopic.pipeInput(new TestRecord<>("alice", Transaction.builder()
            .amount(-123)
            .ownerId("alice")
            .userId("bob")
            .timestamp(Instant.now().toEpochMilli())
            .build()));

        usersInTopic.pipeInput(new TestRecord<>("alice", User.builder()
            .id("alice")
            .name("Alicia")
            .username("alicia")
            .build()));

        List<KeyValue<String, Event>> events = eventOutTopic.readKeyValuesToList();
        assertEquals(1, events.size());
        final KeyValue<String, Event> eventRecord = events.get(0);
        final Event event = eventRecord.value;
        assertTrue("alice".equals(eventRecord.key));
        assertEquals("PaymentSent", event.getEventName());
        assertEquals(-123, event.getAmount());
        assertEquals("alice", event.getFromUserId());
        assertEquals("bob", event.getToUserId());
        assertEquals("Bob", event.getToUserName());
    }

    @Test
    public void shouldProcessPaymentRequest()
    {
        usersInTopic.pipeInput(new TestRecord<>("alice", User.builder()
            .id("alice")
            .name("Alice")
            .username("alice")
            .build()));
        usersInTopic.pipeInput(new TestRecord<>("bob", User.builder()
            .id("bob")
            .name("Bob")
            .username("bob")
            .build()));

        paymentRequestsInTopic.pipeInput(new TestRecord<>("alice", PaymentRequest.builder()
            .id("alice")
            .amount(123)
            .fromUserId("alice")
            .toUserId("bob")
            .timestamp(Instant.now().toEpochMilli())
            .build()));

        List<KeyValue<String, Event>> events = eventOutTopic.readKeyValuesToList();
        assertEquals(1, events.size());
        final KeyValue<String, Event> eventRecord = events.get(0);
        final Event event = eventRecord.value;
        assertTrue("alice".equals(eventRecord.key));
        assertEquals("PaymentRequested", event.getEventName());
        assertEquals(123, event.getAmount());
        assertEquals("alice", event.getFromUserId());
        assertEquals("bob", event.getToUserId());
        assertEquals("Bob", event.getToUserName());
    }
}
