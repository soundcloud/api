#!/bin/bash
set -euf

tag=""

for arg in "$@"; do
  case $arg in
  --tag=*)
    tag="${arg#*=}"
    shift
    ;;
  esac
done

[ -z "$tag" ] && exit 1

env TAG="$tag" \
  docker-compose up -d

teardown() {
  env TAG="$tag" \
    docker-compose down -t 0
}

trap teardown EXIT

env TAG="$tag" \
  docker-compose logs --follow &

sc wait http localhost:8080/-/health
sc wait http localhost:8081/metrics

python3 test_integration.py
