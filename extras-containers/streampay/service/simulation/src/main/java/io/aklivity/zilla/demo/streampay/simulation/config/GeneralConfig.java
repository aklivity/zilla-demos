/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation.config;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.javafaker.Faker;

@Configuration
public class GeneralConfig
{
    @Bean
    public Random random()
    {
        return new Random();
    }

    @Bean
    public Faker faker()
    {
        return new Faker();
    }

}
