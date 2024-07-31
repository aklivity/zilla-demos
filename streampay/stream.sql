-- CREATE TOPIC streampay-commands(
--   type VARCHAR,
     userid VARCHAR,
     requestid VARCHAR,
     amount DOUBLE PRECISION,
     notes VARCHAR
-- )
-- WITH (
--   kafka_topic='commands',
--   PARTITIONS=2,
--   VALUE_FORMAT=avro
-- );
--
--
-- CREATE TOPIC streampay-replies(
--  status VARCHAR,
    correlationid VARCHAR
-- )
-- WITH (
--   PARTITIONS=2,
--   VALUE_FORMAT=avro
-- );


-- CREATE TOPIC streampay-payment-requests(
--  id VARCHAR,
--  fromUserId VARCHAR,
--  fromUserName VARCHAR,
--  toUserId VARCHAR,
--  toUserName VARCHAR,
--  amount DOUBLE PRECISION,
    notes VARCHAR,
    timestamp LONG
-- )
-- WITH (
--   PARTITIONS=2,
--   VALUE_FORMAT=avro
-- );


-- CREATE TOPIC streampay-users(
--  id VARCHAR,
    name VARCHAR,
    username VARCHAR
-- )
-- WITH (
--   PARTITIONS=2,
--   VALUE_FORMAT=avro
-- );


-- CREATE TOPIC streampay-balance-histories(
    balance DOUBLE PRECISION,
    timestamp LONG
-- )
-- WITH (
--   PARTITIONS=2,
--   VALUE_FORMAT=avro
-- );


create function bad_request_status() returns varchar language javascript as $$
    return '400';
$$;

create function success_request_status() returns varchar language javascript as $$
    return '200';
$$;

create function generate_guid() returns varchar language javascript as $$
    var result, i, j;
    result = '';
    for(j=0; j<32; j++) {
        if( j == 8 || j == 12 || j == 16 || j == 20)
          result = result + '-';
        i = Math.floor(Math.random()*16).toString(16).toUpperCase();
        result = result + i;
    }
  return result;
$$;

CREATE TABLE IF NOT EXISTS users(
  *
)
INCLUDE KEY AS user_key
WITH (
    connector='kafka',
    topic='streampay-users',
    properties.bootstrap.server='localhost:9092',
    scan.startup.mode='latest',
    scan.startup.timestamp.millis='140000000'
) FORMAT PLAIN ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

CREATE TABLE IF NOT EXISTS commands
INCLUDE KEY AS key
INCLUDE header'zilla:correlation-id' AS correlation_id
INCLUDE header 'identity' AS owenerid
INCLUDE timestamp as timestamp
WITH (
    connector='kafka',
    topic='streampay-commands',
    properties.bootstrap.server='localhost:9092',
    scan.startup.mode='latest',
    scan.startup.timestamp.millis='140000000'
) FORMAT PLAIN ENCODE AVRO (
    message = 'PaymentCommand',
    schema.registry = 'http://localhost:8081'
);


CREATE MATERIALIZED VIEW IF NOT EXISTS invalid_commands AS
    SELECT bad_request_status() as status, encode(correlation_id, 'escape') as correlationid from commands where key IS NULL OR type NOT IN ('SendPayment', 'RequestPayment');


CREATE MATERIALIZED VIEW IF NOT EXISTS valid_commands AS
    SELECT success_request_status() as status,  encode(correlation_id, 'escape') as correlationid from commands where key IS NOT NULL AND type IN ('SendPayment', 'RequestPayment');


CREATE SINK invalid_replies
FROM invalid_commands
WITH (
    connector='kafka',
    topic='streampay-replies',
    properties.bootstrap.server='localhost:9092',
    primary_key='correlationid'
) FORMAT UPSERT
ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

CREATE SINK valid_replies
FROM valid_commands
WITH (
    connector='kafka',
    topic='streampay-replies',
    properties.bootstrap.server='localhost:9092',
    primary_key='correlationid'
) FORMAT UPSERT
ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

CREATE MATERIALIZED VIEW withdrawals_transaction as
SELECT
    generate_guid() as id,
    owenerid::varchar as owenerid,
    -(cmd.amount) as amount,
    cmd.timestamp as timestamp,
    cmd.userid as userid
FROM
    (
        SELECT
            userid,
            owenerid::varchar as owenerid,
            amount,
            timestamp
        FROM
            commands
        WHERE
        KEY IS NOT NULL
        AND type = 'SendPayment'
    ) as cmd
    LEFT JOIN (
        SELECT
            userid,
            balance
        FROM
            users_balance
    ) AS ub ON cmd.owenerid = ub.userid AND ub.balance >= cmd.amount;


CREATE MATERIALIZED VIEW deposit_transaction as
SELECT
    generate_guid() as id,
    userid as fromUserId,
    -(cmd.amount) as amount,
    cmd.timestamp as timestamp,
     owenerid::varchar as userid
FROM
    (
        SELECT
            userid,
            owenerid::varchar as owenerid,
            amount,
            timestamp
        FROM
            commands
        WHERE
        KEY IS NOT NULL
        AND type = 'SendPayment'
    ) as cmd
    LEFT JOIN (
        SELECT
            userid,
            balance
        FROM
            users_balance
    ) AS ub ON cmd.owenerid = ub.userid AND ub.balance >= cmd.amount;


CREATE MATERIALIZED VIEW request_payment as
SELECT
    generate_guid() as id,
    encode(owenerid, 'escape') as fromUserId,
    u2.username as fromUsername,
    userid as toUserId,
    u1.username as toUsername,
    amount,
    notes
FROM
    commands
JOIN
    users u1 ON u1.id = commands.userid
JOIN
    users u2 ON u2.id = encode(commands.owenerid, 'escape')
WHERE
    key IS NOT NULL
    AND type = 'RequestPayment';


CREATE SINK request_payment_sink
FROM request_payment
WITH (
    connector='kafka',
    topic='streampay-request-payments',
    properties.bootstrap.server='localhost:9092',
    primary_key='id'
) FORMAT UPSERT
ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

drop table users;
drop table commands;

drop MATERIALIZED VIEW valid_commands;
drop MATERIALIZED VIEW invalid_commands;
drop MATERIALIZED VIEW request_payment;

drop sink invalid_replies;
drop sink valid_replies;
drop sink request_payment_sink;


