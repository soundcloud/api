# API Public Release Notes

## Unsupported track transcodings

- fixes https://github.com/soundcloud/api/issues/534 where unplayable tracks were returned as playable but then 403 was returned when trying to play 
- The `/streams` JSON payload **no longer emits keys with null values**. Optional URL fields are **omitted entirely** when not applicable, instead of `"field": null`.

<!--- Remove everything below and start over --->

<<<<<<< dan/repostuser
## Add reposter to activities/feed

- **GET /me/activities**, **GET /me/feed**, **GET /me/activities/tracks**, **GET /me/feed/tracks** (and related stream endpoints) now include a **`reposter`** field on repost activities.
- For items with `type` **track:repost** or **playlist:repost**, the response includes `reposter` with the URN of the user who reposted (e.g. `"soundcloud:users:123"`).
- Non-repost items (e.g. `track`, `playlist`) do not include `reposter`.
- fixes issue: https://github.com/soundcloud/api/issues/20

---
=======
>>>>>>> master

