# API Public Release Notes

## Follow rejection reason in the 422 response

- **PUT /me/followings/{user_urn}** now returns a descriptive `message` in the 422 response when the follow is rejected — for example when the authenticated user has reached the maximum number of followings. Previously the response body carried an empty message.

<!--- Remove everything below and start over --->
