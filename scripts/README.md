# Scripts

## sc-api-auth.mjs

Obtain `client_id` and `client_secret` for your API application via OAuth sign-in and app registration (requires Node.js 18+).

Download the latest:

```bash
curl -fsSL https://raw.githubusercontent.com/soundcloud/api/master/scripts/sc-api-auth.mjs -o sc-api-auth.mjs
node sc-api-auth.mjs --name "My App" --description "…" --website "https://example.com"
```

Add **`--remote`** for SSH, CI, or headless environments (pairing-code sign-in instead of a local browser callback).

Usage, options, and troubleshooting: [api-public-auth-cli](https://github.com/soundcloud/api-public-auth-cli).
