#!/bin/bash
set -e

NAMESPACE=zilla-petstore
docker-compose -p $NAMESPACE down --remove-orphans
