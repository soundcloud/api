#!/usr/bin/env bash
set -o allexport

dir=$(dirname "$BASH_SOURCE[0]")
source $dir/default.sh

PUBLIC_API_STRANGLER_SERVER="${DOCKER_IP}:5000"
MEMCACHED_TEST_HOST="$DOCKER_IP"
MEMCACHED_TEST_PORT=11211
ZOOKEEPER_SERVERS="${DOCKER_IP}:2181"

set +o allexport
