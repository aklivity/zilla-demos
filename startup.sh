#!/bin/bash
set -e

REPO=aklivity/zilla-demos
RELEASE_URL="https://github.com/$REPO/releases/download"
DEMO_FOLDER=""
KAFKA_BROKER="kafka"
WORKDIR=$(pwd)

# help text
read -r -d '' HELP_TEXT <<-EOF || :
Usage: ${CMD:=${0##*/}} [-d WORKDIR][--redpanda] demo-name

Operand:
    example.name          The name of the example to use                                 [default: quickstart][string]

Options:
    -d | --workdir        Sets the directory used to download and run the example                             [string]
         --redpanda       Makes the included kafka broker and scripts use Redpanda                           [boolean]
         --help           Print help                                                                         [boolean]

Report a bug: github.com/$REPO/issues/new
EOF
export USAGE="$HELP_TEXT"
exit2 () { printf >&2 "%s:  %s: '%s'\n%s\n" "$CMD" "$1" "$2" "$USAGE"; exit 2; }
check () { { [ "$1" != "$EOL" ] && [ "$1" != '--' ]; } || exit2 "missing argument" "$2"; } # avoid infinite loop

# parse command-line options
set -- "$@" "${EOL:=$(printf '\1\3\3\7')}"  # end-of-list marker
while [ "$1" != "$EOL" ]; do
  opt="$1"; shift
  case "$opt" in

    #defined options
    -d | --workdir       ) check "$1" "$opt"; WORKDIR="$1"; shift;;
         --redpanda      ) KAFKA_BROKER="redpanda";;
         --help          ) printf "%s\n" "$USAGE"; exit 0;;

    # process special cases
    --) while [ "$1" != "$EOL" ]; do set -- "$@" "$1"; shift; done;;   # parse remaining as positional
    --[!=]*=*) set -- "${opt%%=*}" "${opt#*=}" "$@";;                  # "--opt=arg"  ->  "--opt" "arg"
    -[A-Za-z0-9] | -*[!A-Za-z0-9]*) exit2 "invalid option" "$opt";;    # anything invalid like '-*'
    -?*) other="${opt#-?}"; set -- "${opt%$other}" "-${other}" "$@";;  # "-abc"  ->  "-a" "-bc"
    *) set -- "$@" "$opt";;                                            # positional, rotate to the end
  esac
done; shift

# pull the demo folder from the end of the params and set defaults
operand=$*
DEMO_FOLDER=${operand//\//}
[[ -z "$DEMO_FOLDER" ]] && DEMO_FOLDER="petstore"
[[ -z "$VERSION" ]] && VERSION=$(curl -s https://api.github.com/repos/$REPO/releases/latest | grep -i "tag_name" | awk -F '"' '{print $4}')

if ! [[ -x "$(command -v docker)" ]]; then
    echo "Docker is required to run this setup."
    exit
fi

echo "==== Starting Zilla Demo $DEMO_FOLDER at $WORKDIR ===="

! [[ -d "$WORKDIR" ]] && echo "Error: WORKDIR must be a valid directory." && exit2
if [[ -d "$WORKDIR" && ! -d "$WORKDIR/$DEMO_FOLDER" ]]; then
    echo "==== Downloading $RELEASE_URL/$VERSION/$DEMO_FOLDER.tar.gz to $WORKDIR ===="
    wget -qO- "$RELEASE_URL"/"$VERSION"/"$DEMO_FOLDER.tar.gz" | tar -xf - -C "$WORKDIR"
fi

export NAMESPACE="zilla-${DEMO_FOLDER//./-}"
export KAFKA_BROKER=$KAFKA_BROKER
TEARDOWN_SCRIPT=""

cd "$WORKDIR"/"$DEMO_FOLDER"/
chmod u+x teardown.sh
TEARDOWN_SCRIPT="NAMESPACE=$NAMESPACE $(pwd)/teardown.sh"
printf "\n\n"
echo "==== Starting Zilla $DEMO_FOLDER. Use this script to teardown ===="
printf '%s\n' "$TEARDOWN_SCRIPT"
sh setup.sh

printf "\n\n"
echo "==== Check out the README to see how to use this example ==== "
echo "cd $WORKDIR/$DEMO_FOLDER"
echo "cat README.md"
head -n 4 "$WORKDIR"/"$DEMO_FOLDER"/README.md | tail -n 3

printf "\n\n"
echo "==== Finished, use the teardown script(s) to clean up ===="
printf '%s\n' "$TEARDOWN_SCRIPT" "$KAKFA_TEARDOWN_SCRIPT"
printf "\n"
