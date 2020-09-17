#!/usr/bin/env sh

echo ""
echo "## Mixed parameters"
echo "Rails is very liberal in how input from the client is received"

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
  sc_curl_token_form_and_query \
    --form "grant_type=password" \
    --form "username=${sc_api_user}" \
    --form "password=${sc_api_pass}" \
    --form "scope=${scope_requested}"
)

access_token=$(echo "$response" | jq '.access_token // empty' -r)
assert_not_empty "$access_token" "Access token"
