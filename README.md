# Public API Strangler

An implementation of the [_strangler_ pattern](http://martinfowler.com/bliki/StranglerApplication.html) for Mothership's Public API.

Several small features, like rate-limiting and other security checks, are implemented at this layer, rather than in the Mothership codebase.

## Development

See [CONTRIBUTING.md](https://github.com/soundcloud/public-api-strangler/blob/master/CONTRIBUTING.md#making-a-change) for development and contribution guidelines.

## Notes

### Search leaving Mothership

Routing of search requests is controlled via rollout flags. See
[Search Wiki](https://github.com/soundcloud/search/wiki/Search-Firefighting#search-in-public-api-strangler-is-misbehaving)
for instructions on how to change rollout percentage values.

### Rate limiting whitelist

The rate limiting feature makes use of a whitelist of client application URNs
that will never be rate-limited. The source of truth for this whitelist is the
Zookeeper cluster and is managed by the
[Rate Limiting Service](https://github.com/soundcloud/ratelimiting/).
