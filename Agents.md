# AI Agents & API Integration Guide

This document provides essential technical specifications for AI agents, LLMs, and automated tools attempting to integrate with the SoundCloud API.

## Cursor Agent Skills

Structured skills for coding assistants live in [.cursor/skills/](.cursor/skills/README.md):

- **soundcloud-api-integration** — OpenAPI sources, URNs, auth headers, pagination, streaming, rate limits
- **soundcloud-api-auth** — OAuth 2.1 PKCE and Client Credentials
- **soundcloud-api-discovery** — search, related tracks, related artists

Human-oriented prompt templates: [Building with AI](https://developers.soundcloud.com/docs/building-with-ai). Quick reference: [LLM context](https://developers.soundcloud.com/docs/llm-context).

## API Specification
The SoundCloud API is defined via Swagger/OpenAPI. Agents should use the following specification for endpoint discovery and schema validation:

- **Swagger JSON:** [https://developers.soundcloud.com/docs/api/explorer/api.json](https://developers.soundcloud.com/docs/api/explorer/api.json)

---

## Authentication & Credentials

### Obtaining Credentials
Developers can request API credentials (client IDs) through their SoundCloud account settings.
* **Request URL:** [https://soundcloud.com/you/apps/](https://soundcloud.com/you/apps/)
* **Policy:** Currently, SoundCloud limits credentials to **one application per person**.

### Authentication Protocol
SoundCloud requires **OAuth 2.1** for all authenticated requests. 

> ⚠️ **IMPORTANT:** Do **not** use the legacy endpoint `api.soundcloud.com/oauth2/token`. This endpoint is deprecated and does not support OAuth 2.1 requirements.

### Authorization Flow
Agents must follow the **Authorization Code Flow with PKCE** (Proof Key for Code Exchange) as mandated by the OAuth 2.1 standard. 

1.  **Authorization:** Direct the user to the SoundCloud authorization page.
2.  **Token Exchange:** Exchange the code for an access token via compliant endpoints.
3.  **Security:** Always verify the `state` parameter to prevent CSRF.

For detailed logic, refer to the [SoundCloud Authentication Guide](https://developers.soundcloud.com/docs/api/guide#authentication).

---

## Data Retrieval & Streaming

### Modern Streaming Endpoints
**Do not use the legacy `/stream` endpoint.**
* **New Standard:** Use the `/tracks/{track_urn}/streams` endpoint as it only returns 30sec snippets
* **Format:** This endpoint provides modern AAC-based HLS transcodings (e.g., `hls_aac_160_url`), which offer superior quality and reliability compared to legacy streams.

### Cursor-Based Paging (Preferred)
**AI Agents should prioritize cursor-based pagination over static page numbers.** * **Why:** Cursors are more resilient to real-time data changes and offer better performance for deep-scrolling large datasets.
* **Implementation:** Look for the `next_href` field in the API response. Use that URL for the next request, or pass `linked_partitioning=true` with `limit` / `offset` as documented per endpoint.

### Resource URNs

Path parameters use **SoundCloud URNs** (e.g. `soundcloud:tracks:308946187`, `soundcloud:users:948745750`). Numeric ids are deprecated.

### Discovery endpoints

- **Related tracks:** `GET /tracks/{track_urn}/related` — supports `limit`, `offset`, `linked_partitioning`, `access`
- **Related artists:** `GET /users/{user_urn}/related` — same pagination model (see [releases](https://github.com/soundcloud/api/releases))
