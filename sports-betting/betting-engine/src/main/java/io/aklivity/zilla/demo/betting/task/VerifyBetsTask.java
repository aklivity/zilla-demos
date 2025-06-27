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
import java.util.List;
import java.util.Map;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;
import io.aklivity.zilla.demo.betting.model.Bet;
import io.aklivity.zilla.demo.betting.model.Match;
import io.aklivity.zilla.demo.betting.model.User;
import io.aklivity.zilla.demo.betting.model.VerifiedBet;

public final class VerifyBetsTask implements Runnable
{
    private final EngineContext context;

    public VerifyBetsTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaConsumer<String, String> consumer = context.supplyConsumer();
        KafkaProducer<String, String> producer = context.supplyProducer())
        {
            consumer.subscribe(List.of(BET_PLACED_TOPIC));
            Jsonb jsonb = JsonbBuilder.create();

            while (true)
            {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(100)))
                {
                    String key = record.key();
                    String value = record.value();

                    Bet bet = jsonb.fromJson(value, Bet.class);
                    String userId = bet.userId;
                    int eventId = bet.eventId;
                    String team = bet.team;
                    double amount = bet.amount;

                    Match match = context.matches.get(eventId);
                    User user = context.users.get(userId);

                    if (match == null || user == null)
                    {
                        System.out.printf("Timeout waiting for data: userId=%s, eventId=%d%n", userId, eventId);
                        continue;
                    }

                    Map<String, String> odds = match.odds;
                    List<String> teams = match.teams;
                    String eventName = String.join(" vs ", teams);
                    double balance = user.balance;

                    if (balance >= amount && !"COMPLETED".equals(match.status))
                    {
                        double rawOdds = Double.parseDouble(odds.get(team));
                        double decimalOdds = convertToDecimalOdds(rawOdds);
                        double potentialWinnings = amount * decimalOdds;

                        VerifiedBet verifiedBet = new VerifiedBet();
                        verifiedBet.id = key;
                        verifiedBet.eventId = eventId;
                        verifiedBet.userId = userId;
                        verifiedBet.eventName = eventName;
                        verifiedBet.betType = "match_result";
                        verifiedBet.team = team;
                        verifiedBet.amount = amount;
                        verifiedBet.odds = decimalOdds;
                        verifiedBet.potentialWinnings = potentialWinnings;
                        verifiedBet.status = "Pending";
                        verifiedBet.result = "TBD";
                        verifiedBet.createdAt = Instant.now().toString();
                        verifiedBet.settledAt = "TBD";

                        producer.send(new ProducerRecord<>(BET_VERIFIED_TOPIC, userId, jsonb.toJson(verifiedBet)));
                        producer.flush();
                        System.out.println("Bet verified and sent for user: %s event: %d".formatted(userId, eventId));

                        user.balance = balance - amount;
                        producer.send(new ProducerRecord<>(USER_PROFILE_TOPIC, userId, jsonb.toJson(user)));
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
        double odds)
    {
        return odds > 0
            ? (odds / 100.0) + 1.0
            : (100.0 / -odds) + 1.0;
    }
}
