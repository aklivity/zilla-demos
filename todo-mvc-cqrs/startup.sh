#!/bin/bash
set -e

NAMESPACE="${NAMESPACE:-zilla-todo-mvc-demo}"
export ZILLA_VERSION="${ZILLA_VERSION:-0.9.82}"
export KAFKA_BROKER="${KAFKA_BROKER:-kafka}"
export KAFKA_BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVER:-${KAFKA_BROKER}:9092}"
export KAFKA_PORT="${KAFKA_PORT:-9092}"
INIT_KAFKA="${INIT_KAFKA:-true}"

# Start or restart Zilla
if [[ -z $(docker compose -p $NAMESPACE ps -q zilla) ]]; then
  docker compose -p $NAMESPACE -f docker-compose.yaml -f docker-compose.$KAFKA_BROKER.yaml up -d
else
  docker compose -p $NAMESPACE up -d --force-recreate --no-deps zilla todo-command-service
fi
