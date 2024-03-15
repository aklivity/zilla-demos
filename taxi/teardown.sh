#!/bin/bash
set -e

NAMESPACE=zilla-taxi-demo

docker-compose -p $NAMESPACE down --remove-orphans
