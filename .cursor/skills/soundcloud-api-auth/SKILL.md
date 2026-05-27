---
name: soundcloud-api-auth
description: Implements SoundCloud OAuth 2.1 flows — Authorization Code with PKCE and Client Credentials — including token refresh and secure credential storage. Use when adding login, obtaining tokens, or fixing 401 auth errors.
---

# SoundCloud API authentication

## Endpoints

| Purpose | Host |
|---------|------|
| Authorize user | `https://secure.soundcloud.com/authorize` |
| Token exchange / refresh | `https://secure.soundcloud.com/oauth/token` |

Do **not** use `https://api.soundcloud.com/oauth2/token` (deprecated).

## Authorization Code + PKCE (user-delegated)

Use for `/me`, uploads, and private user actions.

1. Generate `code_verifier` (43–128 chars) and `code_challenge` = BASE64URL(SHA256(verifier))
2. Redirect user to authorize URL with `response_type=code`, `client_id`, `redirect_uri`, `code_challenge`, `code_challenge_method=S256`, `state`
3. Exchange `code` at token endpoint with `grant_type=authorization_code`, `code_verifier`, `redirect_uri`
4. Store `access_token`, `refresh_token`, `expires_in` securely (server-side or httpOnly cookie — never in client-visible storage for secrets)

## Client Credentials (public resources only)

```bash
curl -X POST "https://secure.soundcloud.com/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic BASE64_CLIENT_ID_SECRET" \
  --data-urlencode "grant_type=client_credentials"
```

## Refresh (single-use refresh tokens)

```bash
curl -X POST "https://secure.soundcloud.com/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=refresh_token" \
  --data-urlencode "refresh_token=REFRESH_TOKEN" \
  --data-urlencode "client_id=CLIENT_ID" \
  --data-urlencode "client_secret=CLIENT_SECRET"
```

After refresh, discard the old refresh token; the response includes a new one.

## Constraints

- Access token lifetime ~1 hour
- Refresh tokens are **single-use** — persist the new refresh token from each response
- Avoid hammering `client_credentials` — cache access token and refresh instead
- Register redirect URIs at https://soundcloud.com/you/apps/

## Reference

Full flow details: https://developers.soundcloud.com/docs/api/guide#authentication
