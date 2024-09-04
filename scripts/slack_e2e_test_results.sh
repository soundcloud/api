MESSAGE="✅ public-api e2e-tests passed, yay!"

curl \
    -H "content-type: application/json" \
    -X POST \
    -d "{\"channel\": \"#api-team-alerts\", \"message\": \"${MESSAGE}\", \"username\": \"E. II E. Testerson\"}" \
    http://unslacked.int.s-cloud.net/