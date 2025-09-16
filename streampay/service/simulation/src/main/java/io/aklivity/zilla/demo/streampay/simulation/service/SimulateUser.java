/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;

import io.aklivity.zilla.demo.streampay.data.model.User;

@Service
public class SimulateUser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulateUser.class);
    private Map<String, User> users = new HashMap<>();
    private Map<String, User> virtualUsers = new HashMap<>();

    @Value("${users.topic:users}")
    String usersTopic;

    @Autowired
    private Random random;

    @Autowired
    private Faker faker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public User randomVirtualUser()
    {
        String userId = null;
        if (!virtualUsers.isEmpty())
        {
            Object[] keys = virtualUsers.keySet().toArray();
            userId = (String) keys[random.nextInt(keys.length)];
        }
        return virtualUsers.get(userId);
    }

    public User randomRealUser()
    {
        String userId = null;
        if (!users.isEmpty())
        {
            Object[] keys = users.keySet().toArray();
            userId = (String) keys[random.nextInt(keys.length)];
        }
        return users.get(userId);
    }

    public void insertUser(
        String key,
        User user)
    {
        users.put(key, user);
    }

    public void createUser()
    {
        final User virtualUser = createVirtualUser();
        final String userId = virtualUser.getId();
        kafkaTemplate.send(usersTopic, userId, virtualUser);
        LOGGER.info("Virtual User Created - {}", virtualUser.getName());
        virtualUsers.put(userId, virtualUser);
    }

    private User createVirtualUser()
    {
        final String userId = String.format("virtual-user-%d", virtualUsers.size());

        return User.builder()
            .id(userId)
            .name(String.format(faker.name().fullName()))
            .username(userId)
            .build();
    }
}
