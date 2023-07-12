#!/usr/bin/env bash
set -euf

tag=""
tests=()

for arg in "$@"; do
  case $arg in
  --tag=*)
    tag="${arg#*=}"
    shift
    ;;
  --test=*)
    tests+=("${arg#*=}")
    shift
    ;;
  esac
done

[ -z "$tag" ] && exit 1

env TAG="$tag" \
  docker compose up -d

teardown() {
  env TAG="$tag" \
    docker compose down -t 0
}

trap teardown EXIT

env TAG="$tag" \
  docker compose logs --follow &

sc crun -l base-dev:latest -- \
  sc wait http asset_uploads/-/health

sc crun -l base-dev:latest -- \
  sc wait http media_service/-/health

sc crun -l python-3.7:latest -- \
  python3 -m unittest -v "${tests[@]+"${tests[@]}"}"
