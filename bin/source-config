#!/usr/bin/env bash

# Modified from original
# https://github.com/soundcloud/gatekeeper/blob/59d79684ef6418b5381354aa7f11b4b2bf27c6cf/bin/dev-wrap
#
# This script is used to source a configuration file and make it available in
# `Makefile`s and development targets, currently lives inside the project,
# until further abstracted in a specific cd-tool designed for this purpose

set -o errexit

dir=$(cd "$(dirname "$BASH_SOURCE[0]")" && pwd)

ignore-first-field() {
  sep="$1"
  awk -F$sep "{for (i=2; i<NF; i++) printf \$i \"$sep\"; print \$NF}"
}

# parse command line for --config and load configuration file
if [[ "$1" == --config=* ]]; then
  config=$(echo $1 | ignore-first-field '=')
  config_file="$dir/../$config"
fi

if [ -z "$config_file" ]; then
  echo "usage: ${0} --config=<config file>"
  exit 1
fi

if [ ! -f "$config_file" ]; then
  echo "The specified config file \`$config_file\` does not exist."
  exit 1
fi

echo "${0}: Loading configuration from \`$config\`."
set -o allexport
source $config_file
set +o allexport
echo "${0}: Configuration successfully loaded from \`$config\`."

set +o errexit

shift
exec $@
