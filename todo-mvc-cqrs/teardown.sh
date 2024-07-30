#!/bin/bash
set -e

NAMESPACE="${NAMESPACE:-zilla-todo-mvc-demo}"
docker compose -p $NAMESPACE down --remove-orphans
