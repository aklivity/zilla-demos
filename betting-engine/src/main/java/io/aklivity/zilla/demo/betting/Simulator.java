package io.aklivity.zilla.demo.betting;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class Simulator
{
    private static final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVER");
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Integer, Map<String, Object>> matchCache = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> userProfiles = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> betsById = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public static void start()
    {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "simulator");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> produceMatches(producer));
        executor.submit(() -> monitorMatches(consumerProps, producer));
        executor.submit(() -> cacheBetsAndUsers(consumerProps));

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutting down...");
            executor.shutdownNow();
            producer.close();
        }));
    }

    private static void produceMatches(KafkaProducer<String, String> producer)
    {
        int eventId = 100;
        List<String[]> teamsList = List.of(
            new String[]{"Barcelona", "Real Madrid"},
            new String[]{"Man City", "Liverpool"},
            new String[]{"Man Town", "Liver-river"},
            new String[]{"United City", "Liver-river"},
            new String[]{"Team Messi", "Team Jordan"},
            new String[]{"Team Mr Nobody", "Team Mr Somebody"},
            new String[]{"Arsenal", "Chelsea"});

        List<String> sports = List.of("soccer", "football", "hockey", "basketball", "baseball");

        while (true)
        {
            try
            {
                String[] teams = teamsList.get(random.nextInt(teamsList.size()));
                Map<String, Object> match = new LinkedHashMap<>();
                match.put("id", eventId++);
                match.put("status", "SCHEDULED");
                match.put("sport", sports.get(random.nextInt(sports.size())));
                match.put("teams", Arrays.asList(teams[0], teams[1]));
                match.put("time", Instant.now().plusSeconds(60).toString());
                Random random = new Random();

                int homeOdds = 100 + random.nextInt(201);
                int awayOdds = 100 + random.nextInt(201);

                String home = (random.nextBoolean() ? "+" : "-") + homeOdds;
                String away = (random.nextBoolean() ? "+" : "-") + awayOdds;

                match.put("odds", Map.of("home", home, "away", away));

                String json = mapper.writeValueAsString(match);
                producer.send(new ProducerRecord<>("matches", String.valueOf(match.get("id")), json));
                System.out.println("Scheduled Event: " + json);
                producer.flush();

                Thread.sleep(15_000);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private static void monitorMatches(
        Properties consumerProps,
        KafkaProducer<String, String> producer)
    {
        try (KafkaConsumer<String, String> matchConsumer = new KafkaConsumer<>(consumerProps);
             KafkaConsumer<String, String> betConsumer = new KafkaConsumer<>(consumerProps))
        {
            matchConsumer.subscribe(List.of("matches"));
            betConsumer.subscribe(List.of("bet-verified"));

            while (true)
            {
                matchConsumer.poll(Duration.ofMillis(100)).forEach(record ->
                {
                    try
                    {
                        Map<String, Object> match = mapper.readValue(record.value(), Map.class);
                        Integer id = Integer.valueOf(record.key());
                        matchCache.put(id, match);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                });

                betConsumer.poll(Duration.ofMillis(100)).forEach(record ->
                {
                    try
                    {
                        Map<String, Object> bet = mapper.readValue(record.value(), Map.class);
                        betsById.put((String) bet.get("id"), bet);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                });

                for (Map.Entry<Integer, Map<String, Object>> entry : matchCache.entrySet())
                {
                    Map<String, Object> match = entry.getValue();
                    if ("SCHEDULED".equals(match.get("status")))
                    {
                        String timeStr = (String) match.get("time");
                        Instant time = Instant.parse(timeStr);
                        if (Instant.now().isAfter(time))
                        {
                            resolveMatchAndBets(entry.getKey(), match, producer);
                        }
                    }
                }
                Thread.sleep(2000);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void resolveMatchAndBets(
        int eventId,
        Map<String, Object> match,
        KafkaProducer<String, String> producer)
    {
        List<String> teams = (List<String>) match.get("teams");
        String winner = random.nextBoolean() ? "home" : "away";

        match.put("status", "COMPLETED");
        match.put("result", winner);
        try
        {
            producer.send(new ProducerRecord<>("matches", String.valueOf(eventId), mapper.writeValueAsString(match)));
            System.out.printf("Match %d completed. Winner: %s%n", eventId, winner);
            producer.flush();
            matchCache.remove(eventId);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        for (Map<String, Object> bet : betsById.values())
        {
            if ((Integer) bet.get("event_id") != eventId || !"Pending".equalsIgnoreCase((String) bet.get("status")))
            {
                continue;
            }

            String team = winner.equals("home") ? teams.get(0) : teams.get(1);
            String userId = (String) bet.get("user_id");
            boolean won = ((String) bet.get("team")).equalsIgnoreCase(winner);

            bet.put("result", team);
            bet.put("status", won ? "Won" : "Lost");
            bet.put("settled_at", Instant.now().toString());

            try
            {
                producer.send(new ProducerRecord<>("bet-verified", userId, mapper.writeValueAsString(bet)));
                String id = (String) bet.get("id");
                betsById.remove(id);
                System.out.println("Updated bet result for event: " + eventId + ", bet: " + id);
                producer.flush();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            if (won)
            {
                double winnings = ((Number) bet.get("potential_winnings")).doubleValue();
                Map<String, Object> user = userProfiles.get(userId);
                if (user != null)
                {
                    double balance = ((Number) user.get("balance")).doubleValue();
                    user.put("balance", balance + winnings);
                    try
                    {
                        producer.send(new ProducerRecord<>("user-profile", userId, mapper.writeValueAsString(user)));
                        System.out.printf("Credited %.2f to user %s%n", winnings, userId);
                        producer.flush();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private static void cacheBetsAndUsers(
        Properties props)
    {
        try (KafkaConsumer<String, String> userConsumer = new KafkaConsumer<>(props))
        {
            userConsumer.subscribe(List.of("user-profile"));

            while (true)
            {
                ConsumerRecords<String, String> records = userConsumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records)
                {
                    try
                    {
                        Map<String, Object> user = mapper.readValue(record.value(), Map.class);
                        userProfiles.put(record.key(), user);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                Thread.sleep(500);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
