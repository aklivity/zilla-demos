#!/bin/bash
set -e

NAMESPACE=zilla-taxi

docker-compose -p $NAMESPACE down --remove-orphans
