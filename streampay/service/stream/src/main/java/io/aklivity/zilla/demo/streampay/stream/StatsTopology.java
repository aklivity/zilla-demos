/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream;

import java.time.Instant;
import java.util.Map;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.demo.streampay.data.model.Balance;
import io.aklivity.zilla.demo.streampay.data.model.Event;
import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.model.User;
import io.aklivity.zilla.demo.streampay.data.serde.SerdeFactory;

@Component
public class StatsTopology
{
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<PaymentRequest> paymentRequestSerde = SerdeFactory.jsonSerdeFor(PaymentRequest.class, false);
    private final Serde<Transaction> transactionSerde = SerdeFactory.jsonSerdeFor(Transaction.class, false);
    private final Serde<Event> eventSerde = SerdeFactory.jsonSerdeFor(Event.class, false);
    private final Serde<User> userSerde = SerdeFactory.jsonSerdeFor(User.class, false);
    private final Serde<Balance> balanceSerde = SerdeFactory.jsonSerdeFor(Balance.class, false);

    @Value("${payment.requests.topic:payment-requests}")
    String paymentRequestsTopic;

    @Value("${transactions.topic:transactions}")
    String transactionsTopic;

    @Value("${activities.topic:activities}")
    String activitiesTopic;

    @Value("${users.topic:users}")
    String usersTopic;

    @Value("${balances.topic:balances}")
    String balancesTopic;

    @Value("${balances.topic:balance-histories}")
    String balanceHistoriesTopic;

    public StatsTopology()
    {
    }

    @Autowired
    public void buildPipeline(
        StreamsBuilder satsKafkaStreamsBuilder)
    {
        GlobalKTable<String, User> users = satsKafkaStreamsBuilder.globalTable(usersTopic,
            Consumed.with(stringSerde, userSerde), Materialized.with(stringSerde, userSerde));

        final String paymentSent = "PaymentSent";
        final String paymentReceived = "PaymentReceived";
        final String branch = "Branch-";

        final Map<String, KStream<String, Transaction>> transactionBranches = satsKafkaStreamsBuilder.stream(transactionsTopic,
                Consumed.with(stringSerde, transactionSerde))
            .split(Named.as(branch))
            .branch((key, value) -> value.getAmount() < 0, Branched.as(paymentSent))
            .branch((key, value) -> value.getAmount() >= 0, Branched.as(paymentReceived))
            .defaultBranch();

        transactionBranches.get(branch + paymentSent)
            .leftJoin(users, (id, t) -> t.getUserId(),
                (tran, user) ->
                Event.builder()
                .eventName(paymentSent)
                .amount(tran.getAmount())
                .timestamp(Instant.now().toEpochMilli())
                .toUserId(tran.getUserId())
                .toUserName(user.getName())
                .fromUserId(tran.getOwnerId())
                .build())
            .leftJoin(users, (id, e) -> e.getFromUserId(),
                (event, user) ->
                {
                    event.setFromUserName(user.getName());
                    return event;
                })
            .to(activitiesTopic, Produced.with(stringSerde, eventSerde));

        transactionBranches.get(branch + paymentReceived)
            .leftJoin(users, (id, t) -> t.getUserId(),
                (tran, user) ->
                    Event.builder()
                    .eventName(paymentReceived)
                    .amount(tran.getAmount())
                    .timestamp(Instant.now().toEpochMilli())
                    .fromUserId(tran.getUserId())
                    .fromUserName(user.getName())
                    .toUserId(tran.getOwnerId())
                    .build())
            .leftJoin(users, (id, e) -> e.getToUserId(),
                (event, user) ->
                {
                    event.setToUserName(user.getName());
                    return event;
                })
            .to(activitiesTopic, Produced.with(stringSerde, eventSerde));

        satsKafkaStreamsBuilder.stream(paymentRequestsTopic, Consumed.with(stringSerde, paymentRequestSerde))
            .filter((key, value) -> value != null)
            .leftJoin(users, (id, t) -> t.getToUserId(), (req, user) ->
                Event.builder()
                    .eventName("PaymentRequested")
                    .amount(req.getAmount())
                    .timestamp(Instant.now().toEpochMilli())
                    .toUserId(req.getToUserId())
                    .toUserName(user.getName())
                    .fromUserId(req.getFromUserId())
                    .build())
            .join(users, (id, e) -> e.getFromUserId(), (event, user) ->
            {
                event.setFromUserName(user.getName());
                return event;
            })
            .to(activitiesTopic);

        satsKafkaStreamsBuilder.stream(balancesTopic, Consumed.with(stringSerde, balanceSerde))
            .to(balanceHistoriesTopic, Produced.with(stringSerde, balanceSerde));
    }
}
