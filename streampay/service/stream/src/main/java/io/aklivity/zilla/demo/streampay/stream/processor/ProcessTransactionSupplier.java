/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import io.aklivity.zilla.demo.streampay.data.model.Balance;
import io.aklivity.zilla.demo.streampay.data.model.TotalTransaction;
import io.aklivity.zilla.demo.streampay.data.model.Transaction;

public class ProcessTransactionSupplier implements ProcessorSupplier<String, Transaction, String, Transaction>
{
    private String balanceStoreName;
    private String balanceName;
    private String totalTransactionStoreName;
    private String totalTransactionSinkName;
    private String totalTransactionAmountStoreName;
    private String averageTransactionSink;

    public ProcessTransactionSupplier(
        String balanceStoreName,
        String balanceName,
        String totalTransactionStoreName,
        String totalTransactionSinkName,
        String totalTransactionAmountStoreName,
        String averageTransactionSink)
    {
        this.balanceStoreName = balanceStoreName;
        this.balanceName = balanceName;
        this.totalTransactionStoreName = totalTransactionStoreName;
        this.totalTransactionSinkName = totalTransactionSinkName;
        this.totalTransactionAmountStoreName = totalTransactionAmountStoreName;
        this.averageTransactionSink = averageTransactionSink;
    }

    @Override
    public Processor<String, Transaction, String, Transaction> get()
    {
        return new AggregateBalance();
    }

    class AggregateBalance implements Processor<String, Transaction, String, Transaction>
    {
        private ProcessorContext context;
        private KeyValueStore<String, Balance> balanceStore;
        private KeyValueStore<String, Long> totalTransactionStore;
        private KeyValueStore<String, Double> totalTransactionAmountStore;

        @Override
        public void init(
            final ProcessorContext context)
        {
            this.context = context;
            this.balanceStore = context.getStateStore(balanceStoreName);
            this.totalTransactionStore = context.getStateStore(totalTransactionStoreName);
            this.totalTransactionAmountStore = context.getStateStore(totalTransactionAmountStoreName);
        }

        @Override
        public void process(
            Record<String, Transaction> record)
        {
            final Headers balanceRecordHeaders = new RecordHeaders();
            balanceRecordHeaders.add(new RecordHeader("content-type", "application/json".getBytes()));
            final String userId = record.key();
            double amount = -1;
            if (userId != null)
            {
                double currentBalance = balanceStore.get(userId) == null ? 0 : balanceStore.get(userId).getBalance();
                amount = record.value().getAmount();
                final double newBalanceValue = new BigDecimal(Double.sum(currentBalance, amount))
                    .setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                final Balance newBalance = Balance.builder().balance(newBalanceValue).timestamp(record.timestamp()).build();
                final Record<String, Balance> newBalanceRecord = new Record<>(userId,
                    newBalance, record.timestamp(), balanceRecordHeaders);
                context.forward(newBalanceRecord, balanceName);
                balanceStore.put(userId, newBalance);
            }

            if (amount >= 0)
            {
                final String totalTransactionKey = "total-transaction";
                Long totalTransaction = totalTransactionStore.get(totalTransactionKey);
                totalTransaction = totalTransaction != null ? totalTransaction + 1 : 1;
                context.forward(new Record(
                    totalTransactionKey,
                    TotalTransaction.builder().total(totalTransaction.longValue()).timestamp(record.timestamp()).build(),
                    record.timestamp()), totalTransactionSinkName);
                totalTransactionStore.put(totalTransactionKey, totalTransaction);

                final String totalTransactionAmountKey = "total-amount";
                Double totalAmount = totalTransactionAmountStore.get(totalTransactionAmountKey);
                totalAmount = totalAmount != null ? totalAmount + amount : amount;
                context.forward(new Record(totalTransactionKey,
                    String.valueOf(totalAmount.doubleValue() / totalTransaction), record.timestamp()),
                    averageTransactionSink);
                totalTransactionAmountStore.put(totalTransactionAmountKey, totalAmount);
            }
        }
    }
}
