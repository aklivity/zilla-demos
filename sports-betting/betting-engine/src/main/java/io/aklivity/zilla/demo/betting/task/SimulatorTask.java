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
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.aklivity.zilla.demo.betting.EngineContext;
import io.aklivity.zilla.demo.betting.model.Match;

public final class SimulatorTask implements Runnable
{
    private static final List<String> SPORTS = List.of("soccer", "football", "hockey", "basketball", "baseball");
    private static final List<String[]> TEAMS_LIST = List.of(
        new String[]{"Barcelona", "Real Madrid"},
        new String[]{"Manchester City", "Liverpool"},
        new String[]{"Bayern Munich", "Borussia Dortmund"},
        new String[]{"Juventus", "AC Milan"},
        new String[]{"Dallas Cowboys", "San Francisco 49ers"},
        new String[]{"Green Bay Packers", "Chicago Bears"},
        new String[]{"Chicago Bulls", "New York Knicks"},
        new String[]{"New York Yankees", "Boston Red Sox"});

    private final EngineContext context;

    public SimulatorTask(
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

            int eventId = 100;

            while (true)
            {
                String[] teams = TEAMS_LIST.get(random.nextInt(TEAMS_LIST.size()));
                int homeOdds = 100 + random.nextInt(201);
                int awayOdds = 100 + random.nextInt(201);

                Match match = new Match();
                match.id = eventId;
                match.status = "SCHEDULED";
                match.sport = SPORTS.get(random.nextInt(SPORTS.size()));
                match.teams = Arrays.asList(teams[0], teams[1]);
                match.time = Instant.now().plusSeconds(60).toString();
                match.odds = Map.of(
                    "home", (random.nextBoolean() ? "+" : "-") + homeOdds,
                    "away", (random.nextBoolean() ? "+" : "-") + awayOdds);

                producer.send(new ProducerRecord<>(MATCHES_TOPIC, String.valueOf(eventId), jsonb.toJson(match)));
                producer.flush();
                context.matches.put(eventId++, match);
                System.out.println("Scheduled Event: %s : ID: %d".formatted(match.teams ,match.id));
                Thread.sleep(15_000);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
