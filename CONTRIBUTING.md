# Developing Public API Strangler

## Useful `make` targets

* `make precheckin` – run all the tests.
* `make test` – run just the unit tests.
* `make interactive` – start an SBT console.

Set the `USE_CRUN` environment variable to `false` to avoid using crun when possible.

## Running Public API Strangler locally

```
shibboleth show config/production_api.sh.enc > config/production_api.sh
make run
curl "http://localhost:5000/tracks?client_id=$A_VALID_CLIENT_ID"
```

This runs against production servers.

## Running

Running `make run` from the command line should immediately get you up and running with a local instance running 
at [localhost:5000](http://localhost:5000).

## Debugging

Running `make run-debug` from the command line should immediately get you up and running with a local debug instance. 
Then, from IntelliJ go to "edit configuration" and add a "Remote" target on port `5005`. This will attach the IDE's debugger to the running JVM.

## Making a change

Master should always have the version currently deployed. It is fine to commit directly to master, and make sure that the CD pipeline is always green, and that master is always deployed. Pull requests are mostly used for asking for feedback, comments and external contributions, and are not treated as a hard requirement to ship code. Deploy early and often.
