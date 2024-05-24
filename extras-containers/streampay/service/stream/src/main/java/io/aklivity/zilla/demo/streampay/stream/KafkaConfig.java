/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream;

import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafka
public class KafkaConfig
{
    public static final String PAYMENT_STREAMS_BUILDER_BEAN_NAME = "paymentKafkaStreamsBuilder";
    public static final String STATS_STREAMS_BUILDER_BEAN_NAME = "satsKafkaStreamsBuilder";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    @Value("${spring.kafka.security-protocol:SASL_PLAINTEXT}")
    private String securityProtocol;
    @Value("${spring.kafka.sasl-mechanism:SCRAM-SHA-256}")
    private String saslMechanism;
    @Value("${spring.kafka.streams.state.dir:#{null}}")
    private String stateDir;

    private final String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required" +
        " username=\"%s\" password=\"%s\";";
    private final String jaasCfg = String.format(jaasTemplate, "user", "redpanda");

    @Autowired
    private ApplicationContext context;

    public KafkaConfig()
    {
    }

    @Bean(name = PAYMENT_STREAMS_BUILDER_BEAN_NAME)
    public StreamsBuilderFactoryBean paymentKafkaStreamsBuilder()
    {
        Map<String, Object> paymentStreamsConfigProperties = commonStreamsConfigProperties();
        paymentStreamsConfigProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, PAYMENT_STREAMS_BUILDER_BEAN_NAME);
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(paymentStreamsConfigProperties));
    }
    @Bean(name = STATS_STREAMS_BUILDER_BEAN_NAME)
    public StreamsBuilderFactoryBean statsKafkaStreamsBuilder()
    {
        Map<String, Object> statsStreamsConfigProperties = commonStreamsConfigProperties();
        statsStreamsConfigProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, STATS_STREAMS_BUILDER_BEAN_NAME);
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(statsStreamsConfigProperties));
    }

    private Map<String, Object> commonStreamsConfigProperties()
    {
        final Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", this.bootstrapServers);
        props.put("security.protocol", this.securityProtocol);
        props.put("sasl.mechanism", saslMechanism);
        props.put("sasl.jaas.config", jaasCfg);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        props.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class);
        if (this.stateDir != null)
        {
            props.put("state.dir", this.stateDir);
        }

        props.put("commit.interval.ms", 0);
        return props;
    }

    @PostConstruct
    public void checkConnection()
    {
        Map<String, Object> conf = commonStreamsConfigProperties();
        conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
        conf.put(AdminClientConfig.RETRIES_CONFIG, 1);

        DescribeClusterResult describeClusterResult = null;
        try (AdminClient adminClient = AdminClient.create(conf))
        {
            describeClusterResult = adminClient.describeCluster();
        }
        catch (Exception e)
        {
        }

        if (describeClusterResult == null ||
            describeClusterResult.clusterId().isCompletedExceptionally())
        {
            ((ConfigurableApplicationContext) context).close();
        }
    }
}
