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
package io.aklivity.zilla.demo.betting.task;

import static io.aklivity.zilla.demo.betting.EngineContext.BET_PLACED_TOPIC;
import static io.aklivity.zilla.demo.betting.EngineContext.BET_VERIFIED_TOPIC;
import static io.aklivity.zilla.demo.betting.EngineContext.USER_PROFILE_TOPIC;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;

public class VerifyBetsTask implements Runnable
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final EngineContext context;

    public VerifyBetsTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(context.consumerProps);
        KafkaProducer<String, String> producer = new KafkaProducer<>(context.producerProps))
        {
            consumer.subscribe(List.of(BET_PLACED_TOPIC));

            while (!Thread.currentThread().isInterrupted())
            {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(100)))
                {
                    String key = record.key();
                    String value = record.value();
                    Map<String, Object> bet = mapper.readValue(value, Map.class);
                    String userId = (String) bet.get("userId");
                    int eventId = (Integer) bet.get("eventId");
                    String team = (String) bet.get("team");
                    double amount = ((Number) bet.get("amount")).doubleValue();

                    Map<String, Object> match = context.matches.get(eventId);
                    Map<String, Object> user = context.users.get(userId);

                    if (match == null || user == null)
                    {
                        System.out.printf("Timeout waiting for data: userId=%s, eventId=%d%n", userId, eventId);
                        continue;
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
                        verifiedBet.put("result", "TBD");
                        verifiedBet.put("created_at", Instant.now().toString());
                        verifiedBet.put("settled_at", "TBD");

                        String json = mapper.writeValueAsString(verifiedBet);
                        producer.send(new ProducerRecord<>(BET_VERIFIED_TOPIC, userId, json));
                        producer.flush();
                        System.out.println("Bet verified and sent for user: %s event: %d".formatted(userId, eventId));

                        double newBalance = balance - amount;
                        user.put("balance", newBalance);
                        String updatedUserJson = mapper.writeValueAsString(user);
                        producer.send(new ProducerRecord<>(USER_PROFILE_TOPIC, userId, updatedUserJson));
                        producer.flush();
                    }
                    else
                    {
                        System.out.printf("User %s has insufficient balance to place bet of %.2f%n", userId, amount);
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

    private static double convertToDecimalOdds(
        double americanOdds)
    {
        return americanOdds > 0
            ? (americanOdds / 100.0) + 1.0
            : (100.0 / -americanOdds) + 1.0;
    }
}
