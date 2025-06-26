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

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import io.aklivity.zilla.demo.betting.EngineContext;

public class CacheVerifiedBetsTask implements Runnable
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private final EngineContext context;

    public CacheVerifiedBetsTask(
        EngineContext context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(context.consumerProps))
        {
            consumer.subscribe(List.of(BET_VERIFIED_TOPIC));

            while (!Thread.currentThread().isInterrupted())
            {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(100)))
                {
                    Map<String, Object> bet = mapper.readValue(record.value(), Map.class);
                    context.bets.put((String) bet.get("id"), bet);
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
