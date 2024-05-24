/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import io.aklivity.zilla.demo.streampay.data.model.Balance;
import io.aklivity.zilla.demo.streampay.data.model.Command;
import io.aklivity.zilla.demo.streampay.data.model.PayCommand;
import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.RequestCommand;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.model.User;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;

public class PaymentTopologyTest
{
    private static final String COMMANDS_TOPIC = "commands";
    private static final String COMMANDS_REPLIES_TOPIC = "replies";
    private static final String PAYMENT_REQUESTS_TOPIC = "payment-requests";
    private static final String TRANSACTIONS_TOPIC = "transactions";
    private static final String TOTAL_TRANSACTIONS_TOPIC = "total-transactions";
    private static final String AVERAGE_TRANSACTION_TOPIC = "average-transactions";
    private static final String BALANCES_TOPIC = "balances";
    private static final String USERS_TOPIC = "users";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, Command> commandsInTopic;
    private TestInputTopic<String, Transaction> transactionsInTopic;
    private TestInputTopic<String, User> usersInTopic;
    private TestOutputTopic<String, Balance> balancesOutTopic;
    private TestOutputTopic<String, String> totalTransactionsOutTopic;
    private TestOutputTopic<String, PaymentRequest> requestsOutTopic;

    @BeforeEach
    public void setUp()
    {
        final StreamsBuilder builder = new StreamsBuilder();
        final PaymentTopology stream = new PaymentTopology();
        stream.commandsTopic = COMMANDS_TOPIC;
        stream.commandRepliesTopic = COMMANDS_REPLIES_TOPIC;
        stream.paymentRequestsTopic = PAYMENT_REQUESTS_TOPIC;
        stream.balancesTopic = BALANCES_TOPIC;
        stream.transactionsTopic = TRANSACTIONS_TOPIC;
        stream.usersTopic = USERS_TOPIC;
        stream.averageTransactionTopic = AVERAGE_TRANSACTION_TOPIC;
        stream.totalTransactionsTopic = TOTAL_TRANSACTIONS_TOPIC;
        stream.buildPipeline(builder);
        final Topology topology = builder.build();

        final Properties props = new Properties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        testDriver = new TopologyTestDriver(topology, props);

        transactionsInTopic = testDriver.createInputTopic(TRANSACTIONS_TOPIC,
                new StringSerializer(), new JsonSerializer<>());
        commandsInTopic = testDriver.createInputTopic(COMMANDS_TOPIC,
            new StringSerializer(), new JsonSerializer<>());
        usersInTopic = testDriver.createInputTopic(USERS_TOPIC,
            new StringSerializer(), new JsonSerializer<>());

        StringDeserializer stringDeserializer = new StringDeserializer();
        KafkaJsonDeserializer<Balance> balanceDeserializer = new KafkaJsonDeserializer<>();
        balanceDeserializer.configure(Collections.emptyMap(), false);
        balancesOutTopic = testDriver.createOutputTopic(BALANCES_TOPIC,
            stringDeserializer, balanceDeserializer);

        totalTransactionsOutTopic = testDriver.createOutputTopic(TOTAL_TRANSACTIONS_TOPIC,
            stringDeserializer, stringDeserializer);

        KafkaJsonDeserializer<PaymentRequest> requestDeserializer = new KafkaJsonDeserializer<>();
        requestDeserializer.configure(Collections.emptyMap(), false);
        requestsOutTopic = testDriver.createOutputTopic(PAYMENT_REQUESTS_TOPIC,
            stringDeserializer, requestDeserializer);
    }

    @AfterEach
    public void tearDown()
    {
        testDriver.close();
    }

    @Test
    public void shouldProcessPayCommand()
    {
        final Headers headers = new RecordHeaders(
            new Header[]{
                new RecordHeader("stream-command:operation", "PayCommand".getBytes()),
                new RecordHeader("stream:identity", "user1".getBytes()),
                new RecordHeader("zilla:correlation-id", "1".getBytes()),
                new RecordHeader("idempotency-key", "pay1".getBytes()),
                new RecordHeader(":path", "/pay".getBytes())
            });

        transactionsInTopic.pipeInput(new TestRecord<>("user1", Transaction.builder()
            .amount(123)
            .timestamp(Instant.now().toEpochMilli())
            .build()));

        commandsInTopic.pipeInput(new TestRecord<>("pay1", PayCommand.builder()
            .userId("user2")
            .amount(123)
            .build(), headers));
        List<KeyValue<String, Balance>> balances = balancesOutTopic.readKeyValuesToList();
        assertEquals(3, balances.size());
        List<KeyValue<String, String>> totalValues = totalTransactionsOutTopic.readKeyValuesToList();
        assertEquals(2, totalValues.size());
    }

    @Test
    public void shouldProcessRequestCommand()
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

        final Headers headers = new RecordHeaders(
            new Header[]{
                new RecordHeader("stream-command:operation", "RequestCommand".getBytes()),
                new RecordHeader("stream:identity", "alice".getBytes()),
                new RecordHeader("zilla:correlation-id", "1".getBytes()),
                new RecordHeader("idempotency-key", "request1".getBytes()),
                new RecordHeader(":path", "/request".getBytes())
            });

        commandsInTopic.pipeInput(new TestRecord<>("alice", RequestCommand.builder()
            .userId("bob")
            .amount(123)
            .notes("test")
            .build(), headers));

        List<KeyValue<String, PaymentRequest>> requests = requestsOutTopic.readKeyValuesToList();
        assertEquals(1, requests.size());
    }
}
