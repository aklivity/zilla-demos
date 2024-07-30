#!/bin/bash
set -e

NAMESPACE="${NAMESPACE:-zilla-taxi-demo}"

docker compose -p $NAMESPACE down --remove-orphans
