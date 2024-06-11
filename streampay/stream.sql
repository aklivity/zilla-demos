CREATE SOURCE IF NOT EXISTS users
WITH (
    connector='kafka',
    topic='users',
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
    topic='commands',
    properties.bootstrap.server='localhost:9092',
    scan.startup.mode='latest',
    scan.startup.timestamp.millis='140000000'
) FORMAT PLAIN ENCODE AVRO (
    message = 'PaymentCommand',
    schema.registry = 'http://localhost:8081'
);

CREATE SOURCE IF NOT EXISTS users_balance
WITH (
    connector='kafka',
    topic='users-balance',
    properties.bootstrap.server='localhost:9092',
    scan.startup.mode='latest',
    scan.startup.timestamp.millis='140000000'
) FORMAT PLAIN ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
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
    

CREATE SINK replies
FROM invalid_commands
WITH (
    connector='kafka',
    topic='replies',
    properties.bootstrap.server='localhost:9092',
    primary_key='correlation_id'
) FORMAT UPSERT
ENCODE AVRO (
    schema.registry = 'http://localhost:8081'
);

