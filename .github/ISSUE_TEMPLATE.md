<!--

Thanks for your interest in the SoundCloud Public API.

If you're submitting a bug, please use the following template.
If your issue is a feature request, please include your use-case so that we have all the necessary info.

If you wan to update your app's `redirect_uri`, please refer to our Readme section: https://github.com/soundcloud/api/blob/master/README.md#how-can-i-update-my-apps-redirect_uri

Note: Please do not share any sensitive information such as OAUTH Token or the client_id/client_secret

Note: Please do not use this space for requesting for new API Keys

-->

## Title: Cannot retrieve all tracks for a user
Issue found of: September 20th, 2020

### Endpoint(s):
* `GET /users/{id}/tracks`

### Scope(s):
* None (application is not using authentication i.e., implicit flow)

### Steps to reproduce:
```bash
curl --request GET \
  --url 'https://api.soundcloud.com/users/12345/tracks?limit=1&linked_partitioning=true&client_id=DONT_SHARE_CLIENT_ID'
```

### Expected behaviour:
A list of tracks as described at `https://developers.soundcloud.com/docs/api/reference#users`.

### Actual behaviour:
500 Server Error (consistently)
