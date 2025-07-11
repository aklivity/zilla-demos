/*
 * Copyright 2021-2024 Aklivity Inc
 *
 * Licensed under the Aklivity Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 *   https://www.aklivity.io/aklivity-community-license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.aklivity.zilla.demo.betting;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import io.aklivity.zilla.demo.betting.model.Match;
import io.aklivity.zilla.demo.betting.model.User;
import io.aklivity.zilla.demo.betting.model.VerifiedBet;

public class EngineContext
{
    public static final String BET_PLACED_TOPIC = "bet-placed";
    public static final String BET_VERIFIED_TOPIC = "bet-verified";
    public static final String USER_PROFILE_TOPIC = "user-profile";
    public static final String MATCHES_TOPIC = "matches";

    public final Map<Integer, Match> matches;
    public final Map<String, User> users;
    public final Map<String, VerifiedBet> bets;

    private final Properties consumerProps;
    private final Properties producerProps;

    public EngineContext()
    {
        this.matches = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.bets = new ConcurrentHashMap<>();

        String server = System.getenv("KAFKA_BOOTSTRAP_SERVER");
        consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sports-betting");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    public KafkaProducer<String, String> supplyProducer()
    {
        return new KafkaProducer<>(producerProps);
    }

    public KafkaConsumer<String, String> supplyConsumer()
    {
        return new KafkaConsumer<>(consumerProps);
    }
}
