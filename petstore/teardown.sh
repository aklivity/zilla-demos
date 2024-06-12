#!/bin/bash
set -e

NAMESPACE=${NAMESPACE:-zilla-petstore}

docker-compose -p $NAMESPACE down --remove-orphans
