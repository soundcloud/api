#!/usr/bin/env sh

echo ""
echo "## Client credentials"
echo "https://tools.ietf.org/html/draft-ietf-oauth-v2-13#section-4.4"

base_dir=$(dirname "$0")
. "$base_dir/00-shared.sh"

scope_requested=''

response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=client_credentials"
)

echo "$response"

access_token=$(echo "$response" | jq '.access_token // empty' -r)
assert_not_empty "$access_token" "Access token"

scope_granted=$(echo "$response" | jq '.scope' -r)
assert_equal "$scope_granted" "$scope_requested" "Scope granted"

refresh_token=$(echo "$response" | jq '.refresh_token // empty' -r)
test_refresh "$refresh_token" "$scope_requested"
