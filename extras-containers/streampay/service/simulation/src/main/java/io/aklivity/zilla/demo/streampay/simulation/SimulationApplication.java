/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@SuppressWarnings({"HideUtilityClassConstructor"})
public class SimulationApplication
{
    public static void main(
        String[] args)
    {
        SpringApplication.run(SimulationApplication.class, args);
    }
}
