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


CREATE SOURCE IF NOT EXISTS users
WITH (
    connector='kafka',
    topic='streampay-users',
    properties.bootstrap.server='localhost:9092',
    scan.startup.mode='latest',
    scan.startup.timestamp.millis='140000000'
) FORMAT PLAIN ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

CREATE SOURCE IF NOT EXISTS commands
INCLUDE header 'idempotency-key' AS key
INCLUDE header'zilla:correlation-id' AS correlation_id
INCLUDE header 'stream:identity' AS owener_id
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
    SELECT bad_request_status() as status, correlationid::varchar as correlationid from commands where key IS NULL OR type NOT IN ('PayCommand', 'RequestCommand');


CREATE MATERIALIZED VIEW IF NOT EXISTS valid_commands AS
    SELECT success_request_status() as status, correlationid::varchar as correlationid from commands where key IS NOT NULL AND type IN ('PayCommand', 'RequestCommand');

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
    cmd.userid as owenerid,
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
        AND type = 'PayCommand'
    ) as cmd
    LEFT JOIN (
        SELECT
            userid,
            balance
        FROM
            users_balance
    ) AS ub ON cmd.owenerid = ub.userid AND ub.balance >= cmd.amount;

