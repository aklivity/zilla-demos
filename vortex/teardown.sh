#!/bin/bash

NAMESPACE="${NAMESPACE:-zilla-vortex-demo}"

docker compose -p $NAMESPACE down --remove-orphans
