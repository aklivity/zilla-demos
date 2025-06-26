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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;

public class MatchBetsTask implements Runnable
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final EngineContext context;

    public MatchBetsTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(context.producerProps))
        {
            while (!Thread.currentThread().isInterrupted())
            {
                for (Map.Entry<Integer, Map<String, Object>> entry : context.matches.entrySet())
                {
                    Map<String, Object> match = entry.getValue();
                    if ("SCHEDULED".equals(match.get("status")))
                    {
                        Instant time = Instant.parse((String) match.get("time"));
                        if (Instant.now().isAfter(time))
                        {
                            resolveMatch(entry.getKey(), match, producer);
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
        Map<String, Object> match,
        KafkaProducer<String, String> producer)
    {
        List<String> teams = (List<String>) match.get("teams");
        String winnerSide = context.random.nextBoolean() ? "home" : "away";
        String winningTeam = winnerSide.equals("home") ? teams.get(0) : teams.get(1);

        match.put("status", "COMPLETED");
        match.put("result", winnerSide);

        try
        {
            producer.send(new ProducerRecord<>(MATCHES_TOPIC, String.valueOf(eventId), mapper.writeValueAsString(match)));
            System.out.printf("Match %d completed. Winner: %s%n", eventId, winningTeam);
            producer.flush();
            context.matches.remove(eventId);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        for (Map<String, Object> bet : new ArrayList<>(context.bets.values()))
        {
            if ((Integer) bet.get("event_id") != eventId || !"Pending".equalsIgnoreCase((String) bet.get("status")))
            {
                continue;
            }

            String userId = (String) bet.get("user_id");
            boolean won = ((String) bet.get("team")).equalsIgnoreCase(winnerSide);
            bet.put("result", winningTeam);
            bet.put("status", won ? "Won" : "Lost");
            bet.put("settled_at", Instant.now().toString());

            try
            {
                producer.send(new ProducerRecord<>(BET_VERIFIED_TOPIC, userId, mapper.writeValueAsString(bet)));
                context.bets.remove(bet.get("id"));
                System.out.printf("Updated bet for event %d: %s%n", eventId, bet.get("id"));
                producer.flush();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            if (won)
            {
                creditUser(userId, bet, producer);
            }
        }
    }

    private void creditUser(
        String userId,
        Map<String, Object> bet,
        KafkaProducer<String, String> producer)
    {
        Map<String, Object> user = context.users.get(userId);
        if (user != null)
        {
            double winnings = ((Number) bet.get("potential_winnings")).doubleValue();
            double balance = ((Number) user.get("balance")).doubleValue();
            user.put("balance", balance + winnings);

            try
            {
                producer.send(new ProducerRecord<>(USER_PROFILE_TOPIC, userId, mapper.writeValueAsString(user)));
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
