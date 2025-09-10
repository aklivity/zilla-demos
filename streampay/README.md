# StreamPay App Demo

Streampay a web-based payments app that demonstrates how Zilla integrates seamlessly with Kafka or Redpanda.

This StreamPay app demo consists these main components:

- Redpanda/Apache Kafka.
- Redpanda Console/Kafbat.
- Event processing service written using Spring Boot.
- Zilla API Gateway that hosts both app web interface and APIs.
- StreamPay app UI

## Getting Started

### Requirements

* [Node.js](http://nodejs.org/)
* [Docker](https://www.docker.com/)


### 1. Build the stack

Build the images from scratch (ignores cache):

```bash
docker-compose build --no-cache
```

### 2. Start the stack

#### Using Redpanda

Launch all services in the background with Redpanda:

```bash
docker compose up -d
```

#### Using Apache Kafka

Launch all services in the background with Redpanda:

```bash
docker compose --profile kafka up -d
```

### 3. UI

> 🖥️ App UI is available at: **[http://localhost:8081](http://localhost:8081)**

![screenshot](./assets/screenshot.png)

Click on login and use one of the option to authenticate.

### 4. Stop the stack

#### Using Redpanda

Shut down and clean up resources:

```bash
docker compose down
```

#### Using Apache Kafka

Shut down and clean up resources:

```bash
docker compose --profile kafka down
```

## Event streaming topics

Following topics are used:

- `commands` - This topics get populated by Zilla API Gateway and responsible for processing commands
such as `PayCommand`, `RequestCommand`.
- `replies` - HTTP response for processed command should be posted to this topic for correlated response.
- `transactions` - Stores information about about each transaction between users.
- `activities` - Event sourcing topic that logs all the activities in the system between users.
- `balances` - Tracks latest balance of a user comping from transactions table.
- `payment-requests` - Store payments requested by the user.
- `users` - Stores information about users(logcompacted topic).

## Redpanda Console/Kafbat
Console gives you a simple, interactive approach for gaining visibility into your topics, masking data, managing consumer groups, and exploring 
real-time data with time-travel debugging. 

You can access it at http://localhost:8080.

## Event Processing Service
This service responsible for processing commands such as `PayCommand`, `RequestCommand` and producing messages to the appropriate topics. 

It also has statistic topologies that builds activities, statistics out of topics such as `transactions`, and `payment-requests`

**Docker Images: `streampay-stream` & `streampay-simulation`**

## StreamPay UI
This app is build using `Vue.js` and `Quasar` frameworks and contains user authentication component as well
which uses Auth0 platform.

App UI is available at: **[http://localhost:8081](http://localhost:8081)**

## AsyncAPI Specifications

The app is defined by HTTP & Kafka specs:

- [`asyncapi-http.yaml`](./zilla/asyncapi-http.yaml)  
  Defines the HTTP endpoints, SSE channels, and message payloads.

- [`asyncapi-kafka.yaml`](./zilla/asyncapi-kafka.yaml)  
  Defines the Kafka topics for publishing/consuming bet-related messages.

- [`asyncapi-redpanda.yaml`](./zilla/asyncapi-redpanda.yaml)  
  Defines the Redpanda topics for publishing/consuming bet-related messages.

## Zilla API Gateway
Zilla API Gateway hosts both app UI and APIs. 

| Protocol | Method | Endpoint              | Topic                |
|----------|--------|-----------------------|----------------------|
| SSE      | GET    | /activities           | activities           |
| SSE      | GET    | /payment-requests     | payment-requests     |
| SSE      | GET    | /current-balance      | balances             |
| SSE      | GET    | /total-transactions   | total-transactions   |
| SSE      | GET    | /average-transactions | average-transactions |
| SSE      | GET    | /balance-histories    | balance-histories    |
| HTTP     | POST   | /pay                  | commands             |
| HTTP     | POST   | /request              | commands             |
| HTTP     | PUT    | /current-user         | users                |
| HTTP     | GET    | /users                | users                |

---
