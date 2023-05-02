/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation;

import java.time.Instant;
import java.util.UUID;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.model.User;
import io.aklivity.zilla.demo.streampay.data.serde.SerdeFactory;
import io.aklivity.zilla.demo.streampay.simulation.processor.UserProcessor;
import io.aklivity.zilla.demo.streampay.simulation.service.SimulateUser;

@Component
public class SimulationTopology
{
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<PaymentRequest> paymentRequestSerde = SerdeFactory.jsonSerdeFor(PaymentRequest.class, false);
    private final Serde<Transaction> transactionSerde = SerdeFactory.jsonSerdeFor(Transaction.class, false);

    private final Serde<User> userSerde = SerdeFactory.jsonSerdeFor(User.class, false);

    @Value("${payment.requests.topic:payment-requests}")
    String paymentRequestsTopic;

    @Value("${transactions.topic:transactions}")
    String transactionsTopic;

    @Value("${users.topic:users}")
    String usersTopic;

    @Autowired
    private SimulateUser simulateUser;

    public SimulationTopology()
    {
    }

    @Autowired
    public void buildPipeline(
        StreamsBuilder simulationKafkaStreamsBuilder)
    {
        simulationKafkaStreamsBuilder.stream(usersTopic, Consumed.with(stringSerde, userSerde))
                .filter((key, value) -> !key.startsWith("virtual-user") && value != null)
                .process(() -> new UserProcessor(simulateUser));

        simulationKafkaStreamsBuilder.stream(paymentRequestsTopic, Consumed.with(stringSerde, paymentRequestSerde))
            .filter((key, value) -> value != null && value.getToUserId().startsWith("virtual-user"))
            .map((key, value) -> new KeyValue(value.getFromUserId(), Transaction.builder()
                .id(UUID.randomUUID())
                .ownerId(value.getFromUserId())
                .userId(value.getToUserId())
                .amount(value.getAmount())
                .timestamp(Instant.now().toEpochMilli())
                .build()))
            .to(transactionsTopic, Produced.with(stringSerde, transactionSerde));
        System.out.println(simulationKafkaStreamsBuilder.build().describe());
    }
}
