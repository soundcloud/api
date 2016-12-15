# Developing Public API Strangler

## Useful `make` targets

* `make precheckin` – run all the tests.
* `make test` – run just the unit tests.
* `make interactive` – start an SBT console.

Set the `USE_CRUN` environment variable to `false` to avoid using crun when possible.

## Running Public API Strangler locally

```
make run
curl "https://api.soundcloud.com/users/49416?client_id=$A_VALID_CLIENT_ID"
```

This runs against production servers.

## Making a change

Master should always have the version currently deployed. It is fine to commit directly to master, and make sure that the CD pipeline is always green, and that master is always deployed. Pull requests are mostly used for asking for feedback, comments and external contributions, and are not treated as a hard requirement to ship code. Deploy early and often.
