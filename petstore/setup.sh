#!/bin/bash
set -e

NAMESPACE=${NAMESPACE:-zilla-petstore}

# Start or restart Zilla
if [[ -z $(docker compose -p $NAMESPACE ps -q zilla) ]]; then
  docker compose -p $NAMESPACE up -d
else
  docker compose -p $NAMESPACE up -d --force-recreate --no-deps zilla
fi
