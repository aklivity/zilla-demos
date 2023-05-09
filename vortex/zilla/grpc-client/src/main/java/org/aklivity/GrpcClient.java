package org.aklivity;

import com.google.gson.Gson;
import example.Demo;
import example.DemoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.aklivity.model.Events;

import java.util.Collections;
import java.util.Properties;

public class GrpcClient
{
    private final static String TOPIC = "events";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) throws InterruptedException
    {
        final Consumer<Long, String> consumer = createConsumer();

        final ManagedChannel channel = ManagedChannelBuilder.forAddress(
                "localhost", 8080).usePlaintext().build();

        DemoServiceGrpc.DemoServiceBlockingStub stub = DemoServiceGrpc.newBlockingStub(channel);

        System.out.println("gRPC Client Started!");

        runConsumer(stub, consumer);

        channel.shutdown();
        consumer.close();

        System.out.println("I'm DONE waiting!");
    }

    private static Consumer<Long, String> createConsumer()
    {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        final Consumer<Long, String> consumer =
                new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    static void runConsumer(DemoServiceGrpc.DemoServiceBlockingStub stub, Consumer<Long, String> consumer) throws InterruptedException
    {
        final int giveUp = 1000;   int noRecordsCount = 0;

        while (true)
        {
            final ConsumerRecords<Long, String> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count()==0)
            {
                noRecordsCount++;
                if (noRecordsCount > giveUp)
                {
                    System.out.println("I'm still waiting for new events");
                    //break;
                }
                else
                    continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(), record.value(),
                        record.partition(), record.offset());
                Events events = new Gson().fromJson(record.value(), Events.class);
                stub.demoUnary(Demo.DemoMessage.newBuilder().setMessage(events.getGreeting()).build());
            });

            consumer.commitAsync();
        }
    }
}