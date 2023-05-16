package io.aklivity;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import example.Demo;
import io.aklivity.model.Events;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.io.IOException;

public class KafkaEventTranslator
{
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    private final static String HTTP_TOPIC = "http_messages";
    private final static String GRPC_TOPIC = "grpc_messages";
    private final static String SSE_TOPIC = "sse_messages";
    private final static String GRPC_EXCHANGE_TOPIC = "grpc_exchanges";

    public static void main(String[] args) throws InterruptedException
    {
        ArrayList<String> topicList = new ArrayList<>();
        topicList.add(HTTP_TOPIC);
        topicList.add(GRPC_EXCHANGE_TOPIC);

        final Producer<String, byte[]> gRPCProtoProducer = createGRPCProducer();
        final Producer<String, String> sSEProducer = createSSEProducer();
        final Consumer<String, String> eventConsumer = createConsumer(topicList);

        System.out.println("Kafka Translator Started!");

        runProcess(eventConsumer, gRPCProtoProducer, sSEProducer);

        eventConsumer.close();
        gRPCProtoProducer.close();
        sSEProducer.close();
    }

    static Producer<String, byte[]> createGRPCProducer()
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        return new KafkaProducer<String, byte[]>(props);
    }

    static Producer<String, String> createSSEProducer()
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<String, String>(props);
    }

    static Consumer<String, String> createConsumer(
        ArrayList<String> topicList)
    {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        final Consumer<String, String> consumer =
                new KafkaConsumer<>(props);

        consumer.subscribe(topicList);
        return consumer;
    }

    static void runProcess(
        Consumer<String, String> consumer,
        Producer<String, byte[]> producer,
        Producer<String, String> sSEProducer) throws InterruptedException
    {
        while (true)
        {
            final ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(1000);

            consumerRecords.forEach(record -> {
                if (record.topic().equals(HTTP_TOPIC))
                {
                    System.out.printf("sending to GRPC_TOPIC Record:(%s, %d)\n",
                        record.key(), record.offset());
                    Events events = new Gson().fromJson(record.value(), Events.class);
                    producer.send(new ProducerRecord<String, byte[]>(GRPC_TOPIC, record.key(),
                            Demo.DemoMessage.newBuilder()
                                .setName(events.getName())
                                .setColor(events.getColor())
                                .setLoopCount(events.getLoopCount())
                                .build().toByteArray()));
                }
                else if (record.topic().equals(GRPC_EXCHANGE_TOPIC) && record.value() != null)
                {
                    try
                    {
                    System.out.printf("sending to SSE_TOPIC Record:(%s, %d)\n",
                        record.value(), record.offset());
                        Demo.DemoMessage msg = Demo.DemoMessage
                                        .newBuilder()
                                        .mergeFrom(record.value().getBytes(StandardCharsets.UTF_8))
                                        .build();

                        sSEProducer.send(new ProducerRecord<String, String>(
                            SSE_TOPIC, 
                            record.key(),
                            toJson(msg)
                        ));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            consumer.commitAsync();
        }
    }

    public static String toJson(MessageOrBuilder messageOrBuilder) throws IOException {
        return JsonFormat.printer().print(messageOrBuilder);
    }
}
