# Developing Public API Strangler

## Useful `make` targets

* `make precheckin` – run all the tests.
* `make unit-test` – run just the unit tests.
* `make interactive` – start an SBT console.

## Running Public API Strangler locally

```
make -f Makefile.pipeline package && make run
curl "$(docker-ip):5000/my-endpoint"
docker logs -f publicapistrangler_publicapistrangler_1
```

## Making a change

Master should always have the version currently deployed. It is fine to commit directly to master, and make sure that the CD pipeline is always green, and that master is always deployed. Pull requests are mostly used for asking for feedback, comments and external contributions, and are not treated as a hard requirement to ship code. Deploy early and often.

For external contributions (contributions coming from non-maintainers of this system), please talk to us (Core Services) to ensure that we’re aligned on the contribution, and then open a pull request, or—even better—ask to pair with one of us.

Do not merge or deploy changes unless you are a Core Services team member.
