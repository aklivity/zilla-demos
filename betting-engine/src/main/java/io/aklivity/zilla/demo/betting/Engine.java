package io.aklivity.zilla.demo.betting;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class Engine
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, Map<String, Object>> userProfiles = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<String, Object>> matches = new ConcurrentHashMap<>();
    private static final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVER");
    private static final Duration POLL_DURATION = Duration.ofMillis(500);
    private static final long WAIT_TIMEOUT_MS = 5000;
    private static volatile boolean running = true;

    public static void main(String[] args)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down gracefully...");
            running = false;
        }));

        Simulator.start();

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "bet-engine123121");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaConsumer<String, String> matchConsumer = new KafkaConsumer<>(consumerProps);
            KafkaConsumer<String, String> userConsumer = new KafkaConsumer<>(consumerProps);
            KafkaConsumer<String, String> betConsumer = new KafkaConsumer<>(consumerProps);
            KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps))
        {
            matchConsumer.subscribe(Collections.singletonList("matches"));
            userConsumer.subscribe(Collections.singletonList("user-profile"));
            betConsumer.subscribe(Collections.singletonList("bet-placed"));

            Thread matchThread = new Thread(() -> pollAndCache(matchConsumer, matches, "match"), "MatchCacheThread");
            Thread userThread = new Thread(() -> pollAndCache(userConsumer, userProfiles, "user"), "UserCacheThread");

            matchThread.start();
            userThread.start();

            while (running)
            {
                ConsumerRecords<String, String> records = betConsumer.poll(POLL_DURATION);
                for (ConsumerRecord<String, String> record : records)
                {
                    try
                    {
                        processBet(record.key(), record.value(), producer);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Failed to process bet: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            matchThread.join();
            userThread.join();
        }
        catch (Exception e)
        {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Application exited.");
    }

    @SuppressWarnings("unchecked")
    private static void pollAndCache(
        KafkaConsumer<String, String> consumer,
        Map<?, Map<String, Object>> cache,
        String type)
    {
        while (running)
        {
            ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);
            for (ConsumerRecord<String, String> record : records)
            {
                try
                {
                    Map<String, Object> data = mapper.readValue(record.value(), Map.class);
                    if ("match".equals(type))
                    {
                        ((Map<Integer, Map<String, Object>>) cache).put(Integer.parseInt(record.key()), data);
                    }
                    else
                    {
                        ((Map<String, Map<String, Object>>) cache).put(record.key(), data);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Failed to parse " + type + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void processBet(
        String key,
        String value,
        KafkaProducer<String, String> producer) throws Exception
    {
        Map<String, Object> bet = mapper.readValue(value, Map.class);
        String userId = (String) bet.get("userId");
        int eventId = (Integer) bet.get("eventId");
        String team = (String) bet.get("team");
        double amount = ((Number) bet.get("amount")).doubleValue();

        Map<String, Object> match = waitForMatch(eventId, WAIT_TIMEOUT_MS);
        Map<String, Object> user = waitForUser(userId, WAIT_TIMEOUT_MS);

        if (match == null || user == null)
        {
            System.out.printf("Timeout waiting for data: userId=%s, eventId=%d%n", userId, eventId);
            return;
        }

        Map<String, String> odds = (Map<String, String>) match.get("odds");
        List<String> teams = (List<String>) match.get("teams");
        String eventName = String.join(" vs ", teams);
        double balance = ((Number) user.get("balance")).doubleValue();

        if (balance >= amount && !"COMPLETED".equals(match.get("status")))
        {
            double rawOdds = Double.parseDouble(odds.get(team));
            double decimalOdds = convertToDecimalOdds(rawOdds);
            double potentialWinnings = amount * decimalOdds;

            Map<String, Object> verifiedBet = new HashMap<>();
            verifiedBet.put("id", key);
            verifiedBet.put("event_id", eventId);
            verifiedBet.put("user_id", userId);
            verifiedBet.put("event_name", eventName);
            verifiedBet.put("bet_type", "match_result");
            verifiedBet.put("team", team);
            verifiedBet.put("amount", amount);
            verifiedBet.put("odds", decimalOdds);
            verifiedBet.put("potential_winnings", potentialWinnings);
            verifiedBet.put("status", "Pending");
            verifiedBet.put("result", null);
            verifiedBet.put("created_at", Instant.now().toString());
            verifiedBet.put("settled_at", null);

            String json = mapper.writeValueAsString(verifiedBet);
            producer.send(new ProducerRecord<>("bet-verified", userId, json));
            producer.flush();
            System.out.println("Bet verified and sent for user: %s event: %d".formatted(userId, eventId));

            double newBalance = balance - amount;
            user.put("balance", newBalance);
            String updatedUserJson = mapper.writeValueAsString(user);
            producer.send(new ProducerRecord<>("user-profile", userId, updatedUserJson));
            producer.flush();
        }
        else
        {
            System.out.printf("User %s has insufficient balance to place bet of %.2f%n", userId, amount);
        }
    }

    private static double convertToDecimalOdds(
        double americanOdds)
    {
        return americanOdds > 0
            ? (americanOdds / 100.0) + 1.0
            : (100.0 / -americanOdds) + 1.0;
    }

    private static Map<String, Object> waitForUser(
        String userId,
        long timeoutMillis) throws InterruptedException
    {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMillis)
        {
            Map<String, Object> user = userProfiles.get(userId);
            if (user != null)
            {
                return user;
            }
            Thread.sleep(100);
        }
        return null;
    }

    private static Map<String, Object> waitForMatch(
        int eventId,
        long timeoutMillis) throws InterruptedException
    {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMillis)
        {
            Map<String, Object> match = matches.get(eventId);
            if (match != null)
            {
                return match;
            }
            Thread.sleep(100);
        }
        return null;
    }
}
