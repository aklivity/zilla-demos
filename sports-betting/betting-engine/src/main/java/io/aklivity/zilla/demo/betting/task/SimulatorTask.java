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

import static io.aklivity.zilla.demo.betting.EngineContext.MATCHES_TOPIC;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;

public class SimulatorTask implements Runnable
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final EngineContext context;

    public SimulatorTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(context.producerProps))
        {
            int eventId = 100;
            List<String[]> teamsList = List.of(
                new String[]{"Barcelona", "Real Madrid"},
                new String[]{"Manchester City", "Liverpool"},
                new String[]{"Bayern Munich", "Borussia Dortmund"},
                new String[]{"Juventus", "AC Milan"},
                new String[]{"Dallas Cowboys", "San Francisco 49ers"},
                new String[]{"Green Bay Packers", "Chicago Bears"},
                new String[]{"Chicago Bulls", "New York Knicks"},
                new String[]{"New York Yankees", "Boston Red Sox"}
            );
            List<String> sports = List.of("soccer", "football", "hockey", "basketball", "baseball");

            while (!Thread.currentThread().isInterrupted())
            {
                Map<String, Object> match = new LinkedHashMap<>();
                String[] teams = teamsList.get(context.random.nextInt(teamsList.size()));
                match.put("id", eventId);
                match.put("status", "SCHEDULED");
                match.put("sport", sports.get(context.random.nextInt(sports.size())));
                match.put("teams", Arrays.asList(teams[0], teams[1]));
                match.put("time", Instant.now().plusSeconds(60).toString());

                int homeOdds = 100 + context.random.nextInt(201);
                int awayOdds = 100 + context.random.nextInt(201);

                match.put("odds", Map.of(
                    "home", (context.random.nextBoolean() ? "+" : "-") + homeOdds,
                    "away", (context.random.nextBoolean() ? "+" : "-") + awayOdds
                ));

                String json = mapper.writeValueAsString(match);
                producer.send(new ProducerRecord<>(MATCHES_TOPIC, String.valueOf(eventId), json));
                System.out.println("Scheduled Event: " + json);
                context.matches.put(eventId++, match);
                Thread.sleep(15_000);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
