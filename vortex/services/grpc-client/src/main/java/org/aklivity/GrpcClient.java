package org.aklivity;

import com.google.protobuf.Empty;
import example.Demo;
import example.DemoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;

public class GrpcClient
{
    public static void main(String[] args) throws InterruptedException
    {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(
                "zilla", 8080).usePlaintext().build();

        DemoServiceGrpc.DemoServiceBlockingStub stub = DemoServiceGrpc.newBlockingStub(channel);

        System.out.println("gRPC Client Started!");

        Iterator<Demo.DemoMessage> response = stub.demoServerStream(Empty.newBuilder().build());

        while (response.hasNext())
        {
            Demo.DemoMessage message = response.next();
            System.out.println("Found message: " + message);
            stub.demoUnary(Demo.DemoMessage.newBuilder()
                    .setName(message.getName())
                    .setColor(message.getColor())
                    .setLoopCount(message.getLoopCount() + 1)
                    .build());
        }

        channel.shutdown();

        System.out.println("gRPC Client Shutting Down!");
    }
}