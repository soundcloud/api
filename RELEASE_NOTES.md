# API Public Release Notes

## Quiet mode (reveal_stats / reveal_comments)

Tracks now support **quiet mode** attributes so creators can hide stats and comments.

- **`reveal_stats`** (boolean): When `false`, play count and favorite count are hidden on the track. Default is `true`.
- **`reveal_comments`** (boolean): When `false`, comments are hidden on the track. Default is `true`.

You can set these when creating or updating a track (JSON body or form params). They are returned in the track representation. See [API issue #257](https://github.com/soundcloud/api/issues/257).

<!--- Remove everything below and start over --->

