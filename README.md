# Public API Strangler (PAS)

**Service discovery name:** `http.strangler.prod.public-api.srv.db.s-cloud.net`

An implementation of the [_strangler_ pattern](http://martinfowler.com/bliki/StranglerApplication.html) for Mothership's Public API.

Several small features, like rate-limiting and other security checks, are implemented at this layer, rather than in the Mothership codebase.

## Development

See [CONTRIBUTING.md](https://github.com/soundcloud/public-api-strangler/blob/master/CONTRIBUTING.md#making-a-change) for development and contribution guidelines.

[CD Pipeline](https://ci.soundcloud.org/go/pipeline/activity/public-api-strangler).

## Notes

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
