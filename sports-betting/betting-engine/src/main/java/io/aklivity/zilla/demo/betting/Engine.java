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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.aklivity.zilla.demo.betting.task.CacheVerifiedBetsTask;
import io.aklivity.zilla.demo.betting.task.CacheUsersTask;
import io.aklivity.zilla.demo.betting.task.MatchBetsTask;
import io.aklivity.zilla.demo.betting.task.SimulatorTask;
import io.aklivity.zilla.demo.betting.task.VerifyBetsTask;

public class Engine
{
    public static void main(String[] args)
    {
        try (ExecutorService executor = Executors.newFixedThreadPool(5))
        {
            EngineContext context = new EngineContext();
            executor.submit(new CacheUsersTask(context));
            executor.submit(new CacheVerifiedBetsTask(context));
            executor.submit(new MatchBetsTask(context));
            executor.submit(new SimulatorTask(context));
            executor.submit(new VerifyBetsTask(context));

            CountDownLatch stop = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(stop::countDown));
            stop.await();

            executor.shutdownNow();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
