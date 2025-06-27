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

import static io.aklivity.zilla.demo.betting.EngineContext.BET_VERIFIED_TOPIC;
import static io.aklivity.zilla.demo.betting.EngineContext.MATCHES_TOPIC;
import static io.aklivity.zilla.demo.betting.EngineContext.USER_PROFILE_TOPIC;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;
import io.aklivity.zilla.demo.betting.model.Match;
import io.aklivity.zilla.demo.betting.model.User;
import io.aklivity.zilla.demo.betting.model.VerifiedBet;

public final class MatchBetsTask implements Runnable
{
    private final EngineContext context;

    public MatchBetsTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaProducer<String, String> producer = context.supplyProducer())
        {
            Jsonb jsonb = JsonbBuilder.create();
            Random random = new Random();

            while (true)
            {
                for (Map.Entry<Integer, Match> entry : context.matches.entrySet())
                {
                    Match match = entry.getValue();
                    if ("SCHEDULED".equals(match.status))
                    {
                        Instant time = Instant.parse(match.time);
                        if (Instant.now().isAfter(time))
                        {
                            resolveMatch(entry.getKey(), match, producer, jsonb, random);
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
    private void resolveMatch(
        int eventId,
        Match match,
        KafkaProducer<String, String> producer,
        Jsonb jsonb,
        Random random)
    {
        List<String> teams = match.teams;
        String winnerSide = random.nextBoolean() ? "home" : "away";
        String winningTeam = winnerSide.equals("home") ? teams.get(0) : teams.get(1);
        match.status = "COMPLETED";
        match.result = winnerSide;

        try
        {
            producer.send(new ProducerRecord<>(MATCHES_TOPIC, String.valueOf(eventId), jsonb.toJson(match)));
            producer.flush();
            context.matches.remove(eventId);
            System.out.printf("Match %d completed. Winner: %s%n", eventId, winningTeam);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        for (VerifiedBet bet : context.bets.values())
        {
            if (bet.eventId != eventId || !"Pending".equalsIgnoreCase(bet.status))
            {
                continue;
            }

            String userId = bet.userId;
            boolean won = (bet.team).equalsIgnoreCase(winnerSide);
            bet.result = winningTeam;
            bet.status = won ? "Won" : "Lost";
            bet.settledAt = Instant.now().toString();

            try
            {
                producer.send(new ProducerRecord<>(BET_VERIFIED_TOPIC, userId, jsonb.toJson(bet)));
                producer.flush();
                context.bets.remove(bet.id);
                System.out.printf("Updated bet for event %d: %s%n", eventId, bet.id);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            if (won)
            {
                creditUser(userId, bet, producer, jsonb);
            }
        }
    }

    private void creditUser(
        String userId,
        VerifiedBet bet,
        KafkaProducer<String, String> producer,
        Jsonb jsonb)
    {
        User user = context.users.get(userId);
        if (user != null)
        {
            user.balance = user.balance + bet.potentialWinnings;
            try
            {
                producer.send(new ProducerRecord<>(USER_PROFILE_TOPIC, userId, jsonb.toJson(user)));
                producer.flush();
                System.out.printf("Credited %.2f to user %s%n", user.balance, userId);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
