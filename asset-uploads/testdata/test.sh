#!/bin/bash
set -euf -o pipefail

req_health() {
  sc wait http --verbose localhost:8080/-/health
  sc wait http --verbose localhost:9090/-/health
  sc wait http --verbose localhost:9091/-/health
}

req_chunk() {
  local asset_data=$1

  curl --fail --verbose \
    -H "Transfer-Encoding: chunked" \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080/tracks
}

req_large_chunk() {
  dd if=/dev/urandom bs=1M count=50 2>/dev/null |
    curl --verbose \
      -H "Transfer-Encoding: chunked" \
      -F "track[asset_data]=@-;filename=req_large_chunk.wav" \
      -F "track[title]=123" \
      -F "oauth_token=s3cr3t" \
      localhost:8080/tracks
}

req_length() {
  local asset_data=$1

  curl --fail --verbose \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080/tracks
}

req_large_length() {
  dd if=/dev/urandom bs=1M count=50 2>/dev/null |
    curl --verbose \
      -F "track[asset_data]=@-;filename=req_large_chunk.wav" \
      -F "track[title]=123" \
      -F "oauth_token=s3cr3t" \
      localhost:8080/tracks
}

req_large_token() {
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
    localhost:8080/tracks
}

req_admin_metrics() {
  curl --fail --verbose localhost:8081/metrics
}

run() {
  local asset_data=$1
  local token_bytes=256

  req_health
  req_admin_metrics

  req_chunk "$asset_data"
  req_length "$asset_data"
  req_large_token "$asset_data" "$token_bytes"

  req_large_chunk
  req_large_length

  return

  for i in "S3" "PUBLIC_API_STRANGLER" "MOSHIMOSHI" ; do
    req_admin_metrics \
      | grep 'outgoing_http_request'  \
      | grep -c $i
  done

  req_admin_metrics \
    | grep 'incoming_http_request'
}

config=""
asset_data=""

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
    -max-request-bytes=20971520 \
    -targetURL=http://localhost:9090/ &

pid=$!

cleanup() {
  kill -9 $pid
}

trap cleanup EXIT

run "$asset_data" 128
