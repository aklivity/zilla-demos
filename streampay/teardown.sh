#!/bin/bash
set -e

NAMESPACE=zilla-streampay-demo

docker-compose -p $NAMESPACE down --remove-orphans
