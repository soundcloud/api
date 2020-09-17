#!/usr/bin/env sh

APPLICATION_ID=${APPLICATION_ID:-313941}

echo "Setting up credentials for application ${APPLICATION_ID} ..."

creds=$(curl --silent "http://clientapplications.int.s-cloud.net/credentials?application=soundcloud:applications:$APPLICATION_ID" | jq .)
cred=$(echo "$creds" | jq '.collection.items | map(select(.revoked_at == null)) | .[0] | {client_id, client_secret, redirect_uri}')

CLIENT_ID=$(echo "$cred" | jq '.client_id' -r)
CLIENT_SECRET=$(echo "$cred" | jq '.client_secret' -r)
REDIRECT_URI=$(echo "$cred" | jq '.redirect_uri' -r)

SC_API_BASE_URL=${SC_API_BASE_URL:-https://api.soundcloud.com/}

export CLIENT_ID CLIENT_SECRET REDIRECT_URI SC_API_BASE_URL

echo "Client id: ${CLIENT_ID}"
echo "Redirect URI: ${REDIRECT_URI}"
echo "SoundCloud API base URL: ${SC_API_BASE_URL}"
