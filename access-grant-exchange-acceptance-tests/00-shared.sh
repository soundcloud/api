#!/usr/bin/env sh

sc_curl_token() {
  : "${SC_API_BASE_URL:?SoundCloud API base URL must be defined}"

  curl --silent \
    --request POST "${SC_API_BASE_URL}oauth2/token" \
    "$@"
}

sc_curl_token_client_credentials() {
  : "${CLIENT_ID:?Client ID must be defined}"
  : "${CLIENT_SECRET:?Client secret must be defined}"

  sc_curl_token \
    --data "client_id=${CLIENT_ID}" \
    --data "client_secret=${CLIENT_SECRET}" \
    "$@"
}

sc_curl_token_form_and_query() {
  : "${CLIENT_ID:?Client ID must be defined}"
  : "${CLIENT_SECRET:?Client secret must be defined}"

  curl --silent \
    --request POST "https://api.soundcloud.com/oauth2/token?client_id=${CLIENT_ID}" \
    --form "client_secret=${CLIENT_SECRET}" \
    "$@"
}

assert_equal() {
  actual=$1
  expected=$2
  description=$3

  if [ "$expected" = "$actual" ]; then
    echo "+ $description '$expected' matches expected '$actual'."
  else
    echo "! $description '$expected' DOES NOT match expected '$actual'."
  fi
}

assert_not_empty() {
  actual=$1
  description=$2

  if [ -n "$actual" ]; then
    echo "+ $description '$actual' is not empty."
  else
    echo "! $description '$actual' is empty."
  fi
}

test_refresh() {
  refresh_token=$1
  scope_requested=$2

  if [ -n "$refresh_token" ]; then
    echo "Exchanging refresh token ('$refresh_token') ..."
    response=$(
      sc_curl_token \
        --data "grant_type=refresh_token" \
        --data "refresh_token=${refresh_token}"
    )

    scope_granted=$(echo "$response" | jq '.scope' -r)
    assert_equal "$scope_granted" "$scope_requested" "Scope granted"
  else
    echo "No refresh token"
  fi 
}
