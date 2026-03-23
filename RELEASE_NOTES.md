# API Public Release Notes

## List user reposts (tracks and playlists)

- **GET /me/reposts/tracks** – Returns the authenticated user's track reposts (cursor-based pagination, supports `linked_partitioning` and `access`).
- **GET /me/reposts/playlists** – Returns the authenticated user's playlist reposts (cursor-based pagination, supports `linked_partitioning`).
- **GET /users/{user_urn}/reposts/tracks** – Returns a user's track reposts (cursor-based pagination, supports `linked_partitioning` and `access`).
- **GET /users/{user_urn}/reposts/playlists** – Returns a user's playlist reposts (cursor-based pagination, supports `linked_partitioning`).

Implements [api#62](https://github.com/soundcloud/api/issues/62) and [api#379](https://github.com/soundcloud/api/issues/379).

<!--- Remove everything below and start over --->


