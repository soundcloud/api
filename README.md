# public-api-strangler

An implementation of the [Strangler
Pattern](http://martinfowler.com/bliki/StranglerApplication.html) for Mothership's Public API.

## Development

* `make precheckin` – run all the tests.
* `make unit-test` – run just the unit tests.
* `make interactive` – start an SBT console.

## Contributing

Please see
[CONTRIBUTING.md](https://github.com/soundcloud/public-api-strangler/blob/master/CONTRIBUTING.md#making-a-change)
for guidelines for outside contributors.

#### Useful commands while making changes

```
// hack hack hack
make -f Makefile.pipeline package && make run
curl "$(docker-ip):5000/my-endpoint"
docker logs -f publicapistrangler_publicapistrangler_1
```

## Notes

#### Search leaving Mothership

Routing of search requests is controlled via rollout flags. See
[Search Wiki](https://github.com/soundcloud/search/wiki/Search-Firefighting#search-in-public-api-strangler-is-misbehaving)
for instructions on how to change rollout percentage values.

#### Rate limiting whitelist

The rate limiting feature makes use of a whitelist of client application URNs
that will never be rate-limited. The source of truth for this whitelist is the
Zookeeper cluster and is managed by the
[Rate Limiting Service](https://github.com/soundcloud/ratelimiting/).

## FAQ

#### Is this going to replace the current public API?

Yes and no. This will be the first service hit when someone invokes
`api.soundcloud.com`, but it doesn't aim to replace the
[Mothership](http://github.com/soundcloud/soundcloud).

The Mothership and its ecosystem implement a lot of different concerns and
[Bounded Contexts](http://martinfowler.com/bliki/BoundedContext.html). One of
the concerns is to provide a gateway from the internet to internal services,
like search, payments, uploads and streamming. While there is no plan to port
them to this service immediately, new services and huge enough refactorings of
old services should consider this system, and not the mothership, as its main
way out to the internet.

#### Will this become the new public API?

No. If you are interested in what is coming for the API, please reach
out to the platform team:
[platform@soundcloud.com](platform@soundcloud.com). This is just a
smart proxy to the old API.

#### Besides enabling the Strangler Pattern, what are the benefits of this layer?

Several small features, like rate-limiting and other security checks,
are implemented at this layer, without having to touch the mothership
code.

## External resources

The Strangler pattern:

* [_StranglerApplication_, by Martin Fowler](http://martinfowler.com/bliki/StranglerApplication.html)
* [_Legacy Application Strangulation: Case Studies_, by Paul Hammant](http://paulhammant.com/2013/07/14/legacy-application-strangulation-case-studies/)

Finagle:

* [An Introduction to Finagle](http://twitter.github.io/scala_school/finagle.html)
