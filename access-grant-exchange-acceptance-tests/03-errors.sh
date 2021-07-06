#!/usr/bin/env sh

echo ""
echo "## Errors"
echo "https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.3"

base_dir=$(dirname "$0")
. "$base_dir/00-shared.sh"

error_code_expected="invalid_request"
response=$(
  sc_curl_token
)

echo "$response"

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"

error_code_expected="invalid_client"
response=$(
  sc_curl_token \
    --data "client_id=${CLIENT_ID}" \
    --data "client_secret=abc" \
    --data "grant_type=client_credentials"
)

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"

error_code_expected="invalid_grant"
response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=authorization_code" \
    --data "redirect_uri=abc" \
    --data "code=def"
)

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"

error_code_expected="unsupported_grant_type"
response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=password"
)

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"

error_code_expected="unsupported_grant_type"
response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=code_and_token"
)

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"

error_code_expected="invalid_scope"
response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=client_credentials" \
    --data "scope=a+scope+that+is+not+allowed"
)

error_code=$(echo "$response" | jq '.error' -r)
assert_equal "$error_code" "$error_code_expected" "Error code"
