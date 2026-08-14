# API Public Release Notes

## Track BPM and key signature restored for newer tracks

- **GET /tracks/{track_urn}** and other track representations now return `bpm` and `key_signature` for tracks where analysis data is available.
- Fixes https://github.com/soundcloud/api/issues/556

## Progressive download URL removed from GET /tracks/{track_urn}/streams

- **GET /tracks/{track_urn}/streams** no longer returns `http_mp3_128_url`. HLS stream URLs and `preview_mp3_128_url` (for snippets) are unchanged.

<!--- Remove everything below and start over --->
