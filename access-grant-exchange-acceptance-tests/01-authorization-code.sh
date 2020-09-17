#!/usr/bin/env sh

echo ""
echo "## Authorization Code"
echo "https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1"

: "${CLIENT_ID:?Client ID must be defined}"
: "${REDIRECT_URI:?Redirect URI must be defined}"

base_dir=$(dirname "$0")
. "$base_dir/00-shared.sh"

scope_requested='non-expiring'

echo "Authorize application by visiting"
echo "https://soundcloud.com/connect?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=${scope_requested}"

echo "Enter authorization code in the query string:"
read -r authorization_code </dev/tty

echo "Exchanging authorization code '$authorization_code' ..."

response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=authorization_code" \
    --data "redirect_uri=${REDIRECT_URI}" \
    --data "code=${authorization_code}"
)

access_token=$(echo "$response" | jq '.access_token // empty' -r)
assert_not_empty "$access_token" "Access token"

scope_granted=$(echo "$response" | jq '.scope' -r)
assert_equal "$scope_granted" "$scope_requested" "Scope granted"

refresh_token=$(echo "$response" | jq '.refresh_token // empty' -r)
test_refresh "$refresh_token" "$scope_requested"
