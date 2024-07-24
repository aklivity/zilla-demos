#!/bin/bash
set -e

NAMESPACE="${NAMESPACE:-zilla-vortex-demo}"

docker compose build
docker compose -p $NAMESPACE up -d --force-recreate
