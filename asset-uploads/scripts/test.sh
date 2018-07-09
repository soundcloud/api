#!/bin/bash
set -euf -o pipefail

req_chunk() {
  local asset_data=$1

  curl -vf \
    -H "Transfer-Encoding: chunked" \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080
}

req_length() {
  local asset_data=$1

  curl -vf \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080
}

req_length_loop() {
  local asset_data=$1

  for i in {1..10} ; do
    if [ $((i % 2)) == 1 ] ; then
      req_length "$asset_data"
    else
      req_chunk "$asset_data"
    fi
  done
}

req_token_too_large() {
  local asset_data=$1
  local token_bytes=$2

  # Grab a few random bytes to make a large token.
  local large_token
  large_token=$(head -c "$token_bytes" "$asset_data" \
    | openssl enc -base64 \
    | tr -d '\n')

  curl -f \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=$large_token" \
    localhost:8080
}

run() {
  local asset_data=$1
  local token_bytes=256

  req_chunk "$asset_data"
  req_length "$asset_data"
  req_token_too_large "$asset_data" "$token_bytes"

  # TODO: THis is too slow against S3.
  # req_length_loop "$asset_data"
}

for arg in "$@" ; do
  case $arg in
    --config=*)
      config=${arg#*=}
      shift
      ;;
    --asset_data=*)
      asset_data="${arg#*=}"
      shift
      ;;
  esac
done

[ -z "$config" ] && exit 1
[ -z "$asset_data" ] && exit 1

env $(grep -E '^[A-Z]' "$config" | tr '\n' ' ') \
  ./asset-uploads \
    -addr=:8080 \
    -admin-addr=:8081 \
    -moshimoshiAddr=localhost:9091 \
    -targetURL=http://localhost:9090/ &

pid=$!

cleanup() {
  kill -9 $pid
}

trap cleanup EXIT

run "$asset_data" 128
