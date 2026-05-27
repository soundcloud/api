---
name: soundcloud-api-discovery
description: Implements SoundCloud discovery endpoints — track search, related tracks, and related artists — with URN path parameters and linked_partitioning pagination. Use when building recommendations, “similar” features, or search UIs.
---

# SoundCloud API discovery

Requires an access token (`Authorization: OAuth <token>`). Confirm paths and parameters in the [OpenAPI spec](https://developers.soundcloud.com/docs/api/explorer/api.json).

## Search tracks

```
GET https://api.soundcloud.com/tracks?q=QUERY&access=playable&limit=20&linked_partitioning=true
```

Return results and `next_href` for additional pages.

## Related tracks

```
GET https://api.soundcloud.com/tracks/{track_urn}/related?limit=10&access=playable&linked_partitioning=true
```

- `track_urn`: full URN, e.g. `soundcloud:tracks:308946187`
- Response: track collection; paginate via `next_href`

## Related artists

```
GET https://api.soundcloud.com/users/{user_urn}/related?limit=10&linked_partitioning=true
```

- `user_urn`: full URN, e.g. `soundcloud:users:948745750`
- Response: user collection; paginate via `next_href`
- Same pagination model as related tracks (`limit`, `offset`, `linked_partitioning`)

## Implementation checklist

- [ ] Path uses full URN (not numeric id)
- [ ] `linked_partitioning=true` on list endpoints
- [ ] Follow `next_href` until absent
- [ ] Handle `401` and `429`
- [ ] For tracks, filter with `access=playable` when appropriate
- [ ] Token passed as argument / env — not embedded in source

## Example function spec (Python)

When generating code, produce functions that:

1. Accept `access_token` and resource URN as parameters
2. Use `httpx` or `requests`
3. Return `(items, next_href)` tuple
4. Raise clear exceptions on non-2xx responses
