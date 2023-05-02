/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.demo.streampay.simulation.service.SimulatePayment;
import io.aklivity.zilla.demo.streampay.simulation.service.SimulatePaymentRequest;
import io.aklivity.zilla.demo.streampay.simulation.service.SimulateUser;

@Component
public class SimulationTask
{
    @Autowired
    private SimulateUser simulateUser;

    @Autowired
    private SimulatePaymentRequest simulatePaymentRequest;

    @Autowired
    private SimulatePayment simulatePayment;

    @Scheduled(fixedRateString = "${user.creation.rate:8000}")
    public void scheduleVirtualUserCreation()
    {
        simulateUser.createUser();
    }

    @Scheduled(fixedRateString = "${payment.request.virtual.rate:12000}")
    public void schedulePaymentRequestForVirtualUser()
    {
        simulatePaymentRequest.requestPaymentForVirtualUser();
    }

    @Scheduled(fixedRateString = "${payment.request.real.rate:36000}")
    public void schedulePaymentRequestForRealUser()
    {
        simulatePaymentRequest.requestPaymentForRealUser();
    }

    @Scheduled(fixedRateString = "${payment.virtual.rate:10000}")
    public void schedulePaymentForVirtualUser()
    {
        simulatePayment.makePaymentForVirtualUser();
    }

    @Scheduled(fixedRateString = "${payment.real.rate:30000}")
    public void schedulePaymentForRealUser()
    {
        simulatePayment.makePaymentForRealUser();
    }
}
