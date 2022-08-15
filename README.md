# API Public

[![Swagger UI](https://img.shields.io/badge/API-Swagger%20UI-green)](https://api-public-doc.soundcloud.org/)


**Service discovery name:** `http.api.prod.api-public.srv.db.s-cloud.net`

SoundCloud's Public API.

## Development

See [CONTRIBUTING.md](https://github.com/soundcloud/api-public/blob/master/CONTRIBUTING.md#making-a-change) for development and contribution guidelines.

[CD Pipeline](https://ci.soundcloud.org/go/pipeline/activity/api-public).

## Making requests

To get a fresh OAuth token execute `scripts/sc-token <user_id>`. Example, use it with curl to make a request: 
```shell
curl -H  "Authorization: OAuth `scripts/sc-token <user_id>`" "https://api.soundcloud.com/me"
```
<br/>
Authentication process can be found here: [API Guide](https://developers.soundcloud.com/docs/api/guide#authentication)

## Notes

### Multipart requests

All `multipart/form-data` requests are forwarded by [ampelmann](https://github.com/soundcloud/ampelmann/blob/master/ampelmann.json#L94-L115)
to the [`asset-uploads` component](https://github.com/soundcloud/api-public/blob/master/asset-uploads/README.md).

### Rate limiting allowlist

The rate limiting feature makes use of a allowlist of client application URNs
that will never be rate-limited. The source of truth for this allowlist is the
Zookeeper cluster and is managed by the
[Rate Limiting Service](https://github.com/soundcloud/ratelimiting/).

### OAuth access grant exchange acceptance tests

The `./access-grant-exchange-acceptance-tests` directory contains scripts to
support interactive acceptance testing of the supported OAuth access grant
exchange flows. This intention of these tests is mainly to ensure that no
regressions are introduced while extracting the access grant exchange from
Mothership into API Public.

The test suite can be run for a local instance,

```
sc crun --interactive --docker-options '--net=host' base-dev 'export SC_API_BASE_URL=http://localhost:5000/; sh access-grant-exchange-acceptance-tests/test-suite.sh'
```

or for the production system.

```
sc crun --interactive base-dev sh access-grant-exchange-acceptance-tests/test-suite.sh
```

## Release notes

Release Notes are posted to our public repo soundcloud/api to keep users up-to-date.

In case you are introducing breaking changes or changes worth mentioning to the users please:
* fill in the [RELEASE_NOTES.md](RELEASE_NOTES.md) to generate a release with a current changelog. 

**IMPORTANT**: If your PR is a part of a bigger epic, please create a release for the latest PR only and include all the relevant info.

Note: Markdown formatting is preserved. Check the previous [releases](https://github.com/soundcloud/api/releases). 

For any questions reach out to #api-team.
