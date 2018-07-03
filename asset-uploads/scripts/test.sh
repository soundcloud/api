#!/bin/bash -e

./asset-uploads \
  -listenAddr=:8080 \
  -adminListenAddr=:8081 \
  -targetURL=http://localhost:9090/ &

pid=$!

cleanup() {
  kill -9 $pid
}

trap cleanup EXIT

req_chunk() {
  local asset_data=$1

  curl -f \
    -H "Transfer-Encoding: chunked" \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080
}

req_length() {
  local asset_data=$1

  curl -f \
    -F "track[asset_data]=@${asset_data}" \
    -F "track[title]=123" \
    -F "oauth_token=s3cr3t" \
    localhost:8080
}

req_length_loop() {
  local asset_data=$1

  for i in {1..10} ; do
    req_length "$asset_data"
  done
}

if [ ! -e "$1" ] ; then
  echo "Need asset_data fixture"
  exit 1
fi

req_chunk "$1"
req_length "$1"
req_length_loop "$1"