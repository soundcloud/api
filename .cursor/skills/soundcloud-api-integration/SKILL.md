---
name: soundcloud-api-integration
description: Integrates applications with the SoundCloud HTTP API using OAuth 2.1, OpenAPI, and developer docs. Use when building SoundCloud API clients, auth flows, uploads, playback, pagination, or debugging API errors.
---

# SoundCloud API integration

## Authoritative sources

Use these; do not invent endpoints or headers:

| Resource | URL |
|----------|-----|
| OpenAPI JSON | https://developers.soundcloud.com/docs/api/explorer/api.json |
| API Explorer | https://developers.soundcloud.com/docs/api/explorer/open-api |
| API Guide | https://developers.soundcloud.com/docs/api/guide |
| Register app | https://developers.soundcloud.com/docs/api/register-app |
| LLM context | https://developers.soundcloud.com/docs/llm-context |
| Building with AI | https://developers.soundcloud.com/docs/building-with-ai |

## Core rules

1. **API base:** `https://api.soundcloud.com` — **token host:** `https://secure.soundcloud.com`
2. **Auth:** OAuth 2.1. Authorization Code flow requires **PKCE**. Do not use legacy `api.soundcloud.com/oauth2/token`.
3. **API requests:** `Authorization: OAuth <access_token>` unless the OpenAPI operation specifies otherwise.
4. **Resource identifiers:** Use **URNs** in path parameters (e.g. `soundcloud:users:948745750`, `soundcloud:tracks:308946187`). Numeric ids are deprecated.
5. **Tokens:** Access tokens expire ~1 hour. Refresh tokens are **single-use**. Cache tokens; renew with `refresh_token` grant instead of repeated `client_credentials` exchanges.
6. **Client Credentials limits:** 50 tokens / 12h per app, 30 / 1h per IP — cache and refresh.
7. **Pagination:** Prefer `linked_partitioning=true` and follow `next_href` until absent.
8. **Playback:** Use `/tracks/{track_urn}/streams` (not legacy `/stream`). Check `access`: `playable`, `preview`, `blocked`.
9. **Rate limits:** Back off on `429` (including play-stream quota: 15k / 24h per `client_id`).
10. **Secrets:** Never hardcode `client_id`, `client_secret`, or tokens — use env vars or a secrets manager.

## Auth flow selection

| Flow | Use when |
|------|----------|
| Authorization Code + PKCE | User-delegated access (`/me`, uploads, private content) |
| Client Credentials | Public resources only (search, resolve, public playback) |

## Request template

```bash
curl -X GET "https://api.soundcloud.com/ENDPOINT" \
  -H "Authorization: OAuth ACCESS_TOKEN" \
  -H "accept: application/json; charset=utf-8"
```

## Token exchange (Client Credentials)

```bash
curl -X POST "https://secure.soundcloud.com/oauth/token" \
  -H "accept: application/json; charset=utf-8" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic BASE64_CLIENT_ID_SECRET" \
  --data-urlencode "grant_type=client_credentials"
```

## Error handling

- `401` — missing or invalid token
- `429` — rate limited; exponential backoff
- Consult OpenAPI for per-endpoint requirements

## Additional resources

- Discovery (related tracks/artists, search): [soundcloud-api-discovery](../soundcloud-api-discovery/SKILL.md)
- OAuth PKCE walkthrough: [soundcloud-api-auth](../soundcloud-api-auth/SKILL.md)
