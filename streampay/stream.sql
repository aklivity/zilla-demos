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


CREATE TABLE IF NOT EXISTS users(
  *,
  PRIMARY KEY (user_id)
)
INCLUDE KEY AS user_id
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
INCLUDE header 'identity' AS owener_id
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

CREATE TABLE users_balance(
    userid VARCHAR,
    balance DOUBLE PRECISION
);


create function bad_request_status() returns varchar language javascript as $$
    return '400';
$$;

create function success_request_status() returns varchar language javascript as $$
    return '200';
$$;

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
        AND type = 'PayCommand'
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


CREATE MATERIALIZED VIEW payment_request as
SELECT
    generate_guid() as id,
    userid as fromUserId,
    (SELECT usename
     FROM users
     WHERE users.id = commands.userid) AS fromUsername
    owenerid as toUserId,
    (SELECT usename
     FROM users
     WHERE users.id = commands.owenerid) AS toUsername
    amount,
    timestamp,
     owenerid::varchar as userid
FROM
    commands
WHERE KEY IS NOT NULL AND type = 'RequestPayment'


CREATE SINK payment_request_sink
FROM payment_request
WITH (
    connector='kafka',
    topic='streampay-payment-requests',
    properties.bootstrap.server='localhost:9092',
    primary_key='correlationid'
) FORMAT UPSERT
ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

drop table commands;

drop MATERIALIZED VIEW valid_commands;
drop MATERIALIZED VIEW invalid_commands;

drop sink invalid_replies;
drop sink valid_replies;

