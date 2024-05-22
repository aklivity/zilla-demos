#!/bin/bash
set -e

NAMESPACE=zilla-todo-demo
BROKER=kafka


# help text
read -r -d '' USAGE <<-EOF || :
Usage: ${CMD:=${0##*/}} [--redpanda]

Options:
         --redpanda       The script wont try to start a kafka broker        [boolean]
         --help           Print help                                         [boolean]

Report a bug: github.com/$REPO/issues/new
EOF
exit2 () { printf >&2 "%s:  %s: '%s'\n%s\n" "$CMD" "$1" "$2" "$USAGE"; exit 2; }
check () { { [ "$1" != "$EOL" ] && [ "$1" != '--' ]; } || exit2 "missing argument" "$2"; } # avoid infinite loop

# parse command-line options
set -- "$@" "${EOL:=$(printf '\1\3\3\7')}"  # end-of-list marker
while [ "$1" != "$EOL" ]; do
  opt="$1"; shift
  case "$opt" in

    #defined options
         --redpanda ) BROKER=redpanda;;
         --help     ) printf "%s\n" "$USAGE"; exit 0;;
  esac
done; shift

# Start or restart Zilla
if [[ -z $(docker-compose -p $NAMESPACE ps -q zilla) ]]; then
  docker-compose -p $NAMESPACE -f docker-compose.yaml -f docker-compose.$BROKER.yaml up -d
else
  docker-compose -p $NAMESPACE down zilla todo-command-service
  docker-compose -p $NAMESPACE up -d zilla todo-command-service
fi
