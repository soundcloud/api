# Public-API Strangler

An implementation of the [Strangler
Pattern](http://martinfowler.com/bliki/StranglerApplication.html) for the
mothership's Public API.

## Team

* Team: Core Services <core-services@soundcloud.com>
* IRC: #coreservices. Slack: #core-services.
* Telemetry: [Promdash](http://promdash/public-api-strangler)
* Issue Tracker: [Jira Board](https://soundcloud.atlassian.net/secure/RapidBoard.jspa?rapidView=128).

## How to develop locally

Take a look in [CONTRIBUTING.md](CONTRIBUTING.md). This application uses
[boot2docker](http://boot2docker.io/) and
[docker-compose](https://docs.docker.com/compose) to orchestrate its
dependencies. In order to run the application locally, please make sure to have
docker and docker-compose installed. Before commiting, make sure to run `make
precheckin` and that the tests are passing.

Here are some articles on how to get these two installed.

* [Docker compose](https://docs.docker.com/compose/install/)
* [Docker on MacOSX](https://docs.docker.com/installation/mac/)
* [Docker on Ubuntu](https://docs.docker.com/installation/ubuntulinux/)

The precheckin tests uses crun, one of the
[cd-tools](https://github.com/soundcloud/cd-tools) make sure to have this
installed locally. Check the installation documentation in the [project's
README](https://github.com/soundcloud/cd-tools#installation).

### But really, how do I hack on this?

* `while (!done) {`
  * `// hack hack hack`
  * `make -f Makefile package && make run`
  * `curl "$(docker-ip):5000/my-endpoint"`
  * `docker logs -f publicapistrangler_publicapistrangler_1`
* `}`

## How to deploy

Use cd to deploy the application. For the master branch and deployment
pipeline:
[https://ci.dev.s-cloud.net/go/tab/pipeline/history/public-api-strangler](https://ci.dev.s-cloud.net/go/tab/pipeline/history/public-api-strangler)

Please see
[CONTRIBUTING.md](https://github.com/soundcloud/public-api-strangler/blob/master/CONTRIBUTING.md#making-a-change)
for guidelines for outside contributors.

## CI

For the master branch and deployment pipeline:
[https://ci.dev.s-cloud.net/go/tab/pipeline/history/public-api-strangler](https://ci.dev.s-cloud.net/go/tab/pipeline/history/public-api-strangler).

## Search leaving mothership

Routing of search requests is controlled via rollout flags. See
[Search Wiki](https://github.com/soundcloud/search/wiki/Search-Firefighting#search-in-public-api-strangler-is-misbehaving)
for instructions on how to change rollout percentage values.

## Rate limiting whitelist

The ratelimiting feature makes use of a whitelist of client application URNs
that will never be rate-limited. The source of truth for this whitelist is the
ZooKeeper cluster and is managed by the
[Rate Limiting Service](https://github.com/soundcloud/ratelimiting/).

## FAQ

### What should I read before asking questions?

That's a great question! Try these first:

* [Strangler Application, by Martin Fowler](http://martinfowler.com/bliki/StranglerApplication.html)
* [Legacy Application Strangulation : Case Studies, by Paul Hammant](http://paulhammant.com/2013/07/14/legacy-application-strangulation-case-studies/)
* [An Introduction to Finagle](http://twitter.github.io/scala_school/finagle.html)

### Is this going to replace the current public api?

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

### Will this become the new public API?

No. If you are interested in what is coming for the API, please reach
out to the platform team:
[platform@soundcloud.com](platform@soundcloud.com). This is just a
smart proxy to the old API.

### Besides enablign the Strangler Pattern, what are the benefits of this layer?

Several small features, like rate-limiting and other security checks,
are implemented at this layer, without having to touch the mothership
code.
