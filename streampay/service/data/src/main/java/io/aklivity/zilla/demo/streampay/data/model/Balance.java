/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Balance
{
    double balance;
    long timestamp;
}
