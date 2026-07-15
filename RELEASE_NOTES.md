# API Public Release Notes

## Track storefront

- **PUT /tracks/{track_urn}/storefront** creates or updates the storefront (Artist Storefront) shown on the track page. The request replaces the whole storefront: omitted optional fields (`link_title`, `description`, `price`) are cleared, so always send every value the storefront should keep. The authenticated user must own the track and hold a creator subscription that includes external purchase options. The storefront links to an external page; no payment is processed by SoundCloud.

<!--- Remove everything below and start over --->

## Recently played tracks

- **GET /me/recently-played/tracks** returns the authenticated user's last 25 recently played tracks as full track objects in reverse chronological order, with duplicate tracks omitted (no pagination).


