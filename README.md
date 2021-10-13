# Deprecated

Traffic and code have been moved to [API Public](https://github.com/soundcloud/api-public). Do not make new changes here. This repository will be graveyarded soon.

## Public API Strangler (PAS)

[![Swagger UI](https://img.shields.io/badge/API-Swagger%20UI-green)](https://public-api-doc.soundcloud.org/)


**Service discovery name:** `http.strangler.prod.public-api.srv.db.s-cloud.net`

An implementation of the [_strangler_ pattern](http://martinfowler.com/bliki/StranglerApplication.html) for Mothership's Public API.

Several small features, like rate-limiting and other security checks, are implemented at this layer, rather than in the Mothership codebase.

## Development

See [CONTRIBUTING.md](https://github.com/soundcloud/public-api-strangler/blob/master/CONTRIBUTING.md#making-a-change) for development and contribution guidelines.

[CD Pipeline](https://ci.soundcloud.org/go/pipeline/activity/public-api-strangler).

## Notes

### Multipart requests

All `multipart/form-data` requests are forwarded by [ampelmann](https://github.com/soundcloud/ampelmann/blob/master/ampelmann.json#L94-L115)
to the [`asset-uploads` component](https://github.com/soundcloud/public-api-strangler/blob/master/asset-uploads/README.md).

### Rate limiting allowlist

The rate limiting feature makes use of a allowlist of client application URNs
that will never be rate-limited. The source of truth for this allowlist is the
Zookeeper cluster and is managed by the
[Rate Limiting Service](https://github.com/soundcloud/ratelimiting/).

### OAuth access grant exchange acceptance tests

The `./access-grant-exchange-acceptance-tests` directory contains scripts to
support interactive acceptance testing of the supported OAuth access grant
exchange flows. These intention of these tests is mainly to ensure that no
regressions are introduced while extracting the access grant exchange from
Mothership into Public API Strangler.

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

For any questions reach out to #integrations-team.
