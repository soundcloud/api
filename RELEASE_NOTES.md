# API Public Release Notes

## Sort parameter for user tracks

- **GET /users/{user_urn}/tracks** and **GET /me/tracks** now accept an optional `sort` query parameter (`asc` or `desc`) to control upload-date order. Newest-first (`desc`) remains the default. `direction` and `order` are accepted as aliases.

<!--- Remove everything below and start over --->
