/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"HideUtilityClassConstructor"})
public class PaymentServiceApplication
{
    public static void main(
        String[] args)
    {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
