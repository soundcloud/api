#!/usr/bin/env sh

echo ""
echo "## Resource Owner Password Credentials"
echo "https://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2"

base_dir=$(dirname "$0")
. "$base_dir/00-shared.sh"

scope_requested=''

echo "Username"
read -r sc_api_user < /dev/tty

echo "Password"
stty -echo # Don't echo password
read -r sc_api_pass < /dev/tty
stty echo

echo "Signing in ..."
response=$(
  sc_curl_token_client_credentials \
    --data "grant_type=password" \
    --data "username=${sc_api_user}" \
    --data "password=${sc_api_pass}" \
    --data "scope=${scope_requested}"
)

access_token=$(echo "$response" | jq '.access_token // empty' -r)
assert_not_empty "$access_token" "Access token"

scope_granted=$(echo "$response" | jq '.scope' -r)
assert_equal "$scope_granted" "$scope_requested" "Scope granted"

refresh_token=$(echo "$response" | jq '.refresh_token // empty' -r)
test_refresh "$refresh_token" "$scope_requested"
