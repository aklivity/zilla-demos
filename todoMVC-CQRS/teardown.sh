#!/bin/bash
set -e

NAMESPACE=zilla-todo-demo

docker-compose -p $NAMESPACE down --remove-orphans
