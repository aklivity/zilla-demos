/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream.processor;

import java.util.Map;
import java.util.UUID;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.util.Strings;

import io.aklivity.zilla.demo.streampay.data.model.Balance;
import io.aklivity.zilla.demo.streampay.data.model.Command;
import io.aklivity.zilla.demo.streampay.data.model.PayCommand;
import io.aklivity.zilla.demo.streampay.data.model.PaymentRequest;
import io.aklivity.zilla.demo.streampay.data.model.RequestCommand;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;
import io.aklivity.zilla.demo.streampay.data.model.User;

public class ProcessValidCommandSupplier implements ProcessorSupplier<String, Command, String, Command>
{

    private final String balanceStoreName;
    private final String userStoreName;
    private final String replyTo;
    private final String transactionName;
    private final String paymentRequestsName;

    @FunctionalInterface
    private interface CommandProcessor
    {
        void process(Record<String, Command> record);
    }

    public ProcessValidCommandSupplier(
        String balanceStoreName,
        String userStoreName,
        String replyTo,
        String transactionName,
        String paymentRequestsName)
    {
        this.balanceStoreName = balanceStoreName;
        this.userStoreName = userStoreName;
        this.transactionName = transactionName;
        this.paymentRequestsName = paymentRequestsName;
        this.replyTo = replyTo;
    }

    @Override
    public Processor<String, Command, String, Command> get()
    {
        return new ProcessValidCommand();
    }

    private class ProcessValidCommand implements Processor<String, Command, String, Command>
    {
        private ProcessorContext context;

        private KeyValueStore<String, Balance> balanceStore;
        private KeyValueStore<String, User> userStore;

        private final Map<Class<?>, CommandProcessor> processors =
            Map.of(
                PayCommand.class, this::processPayCommand,
                RequestCommand.class, this::processPaymentRequestCommand
            );

        @Override
        public void init(
            final ProcessorContext context)
        {
            this.context = context;
            this.balanceStore = context.getStateStore(balanceStoreName);
            this.userStore = context.getStateStore(userStoreName);
        }

        @Override
        public void process(
            Record<String, Command> record)
        {
            final Command command = record.value();
            final Headers headers = record.headers();
            final Header idempotencyKey = headers.lastHeader("idempotency-key");

            if (idempotencyKey == null)
            {
                processInvalidCommand(record, "Missing idempotency-key header");
            }
            else
            {
                final CommandProcessor commandProcessor = processors.get(command.getClass());
                if (commandProcessor != null)
                {
                    commandProcessor.process(record);
                }
                else
                {
                    processInvalidCommand(record, "Unsupported command");
                }
            }
        }

        private void processPayCommand(
            Record<String, Command> record)
        {
            final PayCommand payCommand = (PayCommand) record.value();
            final Headers headers = record.headers();
            final Header userId = headers.lastHeader("zilla:identity");
            final Header correlationId = headers.lastHeader("zilla:correlation-id");
            final Headers newResponseHeaders = new RecordHeaders();
            newResponseHeaders.add(correlationId);

            final String userIdValue = new String(userId.value());
            if (validateTransaction(userIdValue, payCommand.getAmount()))
            {
                final Headers transactionRecordHeaders = new RecordHeaders();
                transactionRecordHeaders.add(new RecordHeader("content-type", "application/json".getBytes()));
                final Record<String, Transaction> withdrawalsTransaction = new Record<>(userIdValue,
                    Transaction.builder()
                        .id(UUID.randomUUID())
                        .ownerId(userIdValue)
                        .amount(-payCommand.getAmount())
                        .timestamp(record.timestamp())
                        .userId(payCommand.getUserId())
                        .build(),
                    record.timestamp(), transactionRecordHeaders);

                context.forward(withdrawalsTransaction, transactionName);

                final Record<String, Transaction> depositTransaction = new Record<>(payCommand.getUserId(),
                    Transaction.builder()
                        .id(UUID.randomUUID())
                        .ownerId(payCommand.getUserId())
                        .amount(payCommand.getAmount())
                        .timestamp(record.timestamp())
                        .userId(userIdValue)
                        .build(),
                    record.timestamp(), transactionRecordHeaders);

                context.forward(depositTransaction, transactionName);

                if (payCommand.getRequestId() != null)
                {
                    final Headers paymentRequestsRecordHeaders = new RecordHeaders();
                    paymentRequestsRecordHeaders.add(userId);

                    final Record paymentRequest = new Record(payCommand.getRequestId(),
                        null, record.timestamp(), paymentRequestsRecordHeaders);
                    context.forward(paymentRequest, paymentRequestsName);
                }

                newResponseHeaders.add(":status", "200".getBytes());
                final Record reply = record.withHeaders(newResponseHeaders).withValue(Strings.EMPTY);
                context.forward(reply, replyTo);
            }
            else
            {
                processInvalidCommand(record, "Transaction Failed");
            }
        }

        private void processPaymentRequestCommand(
            Record<String, Command> record)
        {
            final RequestCommand requestCommand = (RequestCommand) record.value();
            final Headers headers = record.headers();
            final Header userId = headers.lastHeader("zilla:identity");
            final Header correlationId = headers.lastHeader("zilla:correlation-id");

            final Headers newResponseHeaders = new RecordHeaders();
            newResponseHeaders.add(correlationId);

            final Headers paymentRequestsRecordHeaders = new RecordHeaders();
            paymentRequestsRecordHeaders.add(new RecordHeader("content-type", "application/json".getBytes()));
            String toUserId = requestCommand.getUserId();
            paymentRequestsRecordHeaders.add(new RecordHeader("zilla:identity", toUserId.getBytes()));

            newResponseHeaders.add(":status", "200".getBytes());
            final Record reply = record.withHeaders(newResponseHeaders).withValue(Strings.EMPTY);
            context.forward(reply, replyTo);

            String fromUserId = new String(userId.value());
            final String fromUserName = userStore.get(fromUserId).getName();
            final String toUserName = userStore.get(toUserId).getName();

            String key = UUID.randomUUID().toString();
            final Record paymentRequest = new Record(key,
                PaymentRequest.builder()
                    .id(key)
                    .fromUserId(fromUserId)
                    .fromUserName(fromUserName)
                    .toUserId(toUserId)
                    .toUserName(toUserName)
                    .amount(requestCommand.getAmount())
                    .notes(requestCommand.getNotes())
                    .timestamp(record.timestamp())
                    .build(),
                record.timestamp(),
                paymentRequestsRecordHeaders);
            context.forward(paymentRequest, paymentRequestsName);
        }

        private void processInvalidCommand(
            Record<String, Command> record,
            String message)
        {
            final Headers headers = record.headers();
            final Header correlationId = headers.lastHeader("zilla:correlation-id");
            final Headers newResponseHeaders = new RecordHeaders();
            newResponseHeaders.add(correlationId);

            newResponseHeaders.add(":status", "400".getBytes());
            final Record reply = record
                .withHeaders(newResponseHeaders)
                .withValue(message);
            context.forward(reply, replyTo);
        }

        private boolean validateTransaction(
            String userId,
            double amount)
        {
            Double currentBalance = balanceStore.get(userId) != null ? balanceStore.get(userId).getBalance() : 0;
            return currentBalance - amount >= 0;
        }
    }
}
