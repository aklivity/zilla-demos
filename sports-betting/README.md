# 🏈 Sports Betting App

This is a fully containerized **Sports Betting App** that uses both **HTTP** and **Kafka** for event-driven interactions powered by **Zilla**, **AsyncAPI** Specs & **Kafka**.

---

## 📦 Stack Overview

The system supports:

- **HTTP endpoints** to place bets and manage users
- **Kafka topics** to stream bet events, verified bets, matches, and user profiles
- **Live events & odds updates** via Server-Sent Events (SSE)
- **AsyncAPI 3.0.0** spec-driven messaging

> 🖥️ App UI is available at: **[http://localhost:3000](http://localhost:3000)**

---

## 🚀 Getting Started

### Requirements

* [Node.js](http://nodejs.org/)
* [Docker](https://www.docker.com/)


### 1. Build the stack

Build the images from scratch (ignores cache):

```bash
docker-compose build --no-cache
```

### 2. Start the stack

Launch all services in the background:

```bash
docker compose up -d
```

### 3. Stop the stack

Shut down and clean up resources:

```bash
docker compose down
```

---

## 📘 AsyncAPI Specifications

The app is defined by two specs:

- [`http-asyncapi.yaml`](./zilla/etc/specs/http-asyncapi.yaml)  
  Defines the HTTP endpoints, SSE channels, and message payloads.

- [`kafka-asyncapi.yaml`](./zilla/etc/specs/kafka-asyncapi.yaml)  
  Defines the Kafka topics for publishing/consuming bet-related messages.

### Main Channels

| Channel Type | Name                 | Description                           |
|--------------|----------------------|---------------------------------------|
| HTTP         | `/bet`               | Place a new bet                       |
| HTTP         | `/user`              | Create a user                         |
| SSE          | `/matches`           | Live match updates                    |
| SSE          | `/bet-verified/{id}` | Stream of verified bets               |
| Kafka Topic  | `bet-placed`         | Produced when a bet is placed         |
| Kafka Topic  | `bet-verified`       | Stream of verified/settled bets       |
| Kafka Topic  | `user-profile`       | User profile updates                  |
| Kafka Topic  | `matches`            | Match updates                         |

---
