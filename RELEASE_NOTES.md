<!--- Remove everything below and start over --->

Changed `cursor` parameter format in `GET /me/activities/tracks`, `GET /me/activities`, `GET /me/activities/all/own`, 
`GET /me/followings/tracks` REST endpoints, the new format is an integer value instead of UUID.
This should not affect clients which use pagination links since they should be treated as opaque links.