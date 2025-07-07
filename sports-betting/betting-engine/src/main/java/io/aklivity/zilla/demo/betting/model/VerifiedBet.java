/*
 * Copyright 2021-2024 Aklivity Inc
 *
 * Licensed under the Aklivity Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 *   https://www.aklivity.io/aklivity-community-license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.aklivity.zilla.demo.betting.model;

import jakarta.json.bind.annotation.JsonbProperty;

public class VerifiedBet
{
    public String id;
    @JsonbProperty("user_id")
    public String userId;
    @JsonbProperty("event_id")
    public int eventId;
    @JsonbProperty("event_name")
    public String eventName;
    @JsonbProperty("bet_type")
    public String betType;
    public String team;
    public String result;
    public double odds;
    public double amount;
    @JsonbProperty("potential_winnings")
    public double potentialWinnings;
    @JsonbProperty("created_at")
    public String createdAt;
    @JsonbProperty("settled_at")
    public String settledAt;
    public String status;
}
