# Developing API Public

## BFF architecture

We recommend reading about our BFFs before making changes to our codebase, especially if your change involved business logic. You can learn more about BFFs (including architectural guidelines and best practices) in the [BFF Documentation](https://eng-doc.soundcloud.org/guidelines/bff).

## Useful `make` targets

* `make precheckin` – run all the tests.
* `make test` – run just the unit tests.
* `make interactive` – start an SBT console.

Set the `USE_CRUN` environment variable to `false` to avoid using sc crun when possible.

## Running locally

### API Public using SBT
```
make run
curl "http://localhost:5000/tracks" -H "Authorization: OAuth ACCESS_TOKEN"
```
This runs against production servers.

### Asset-Uploads and API Public in dockerized containers
The dockerized containers can be started with either `make docker-up-e2e` or `make docker-up-development`.
Sending requests to the dockerized asset-uploads application, will forward requests to the dockerized api-public
application - **all other internal requests will be against production systems**.

Example curl:
```
curl -vi \                                    
-H "Authorization: OAuth `scripts/sc-token <sc-user-id>`" \
-F "track[asset_data]=@<path-to-audio-file>" \
-F "track[sharing]=private" \
-F "track[title]=My Track" \
http://localhost:5005/tracks
```

By default, the log-level for api-public is WARN; to see the DEBUG logs when testing locally, run:
```
curl -X POST http://localhost:5001/-/log-level/DEBUG
```

## Debugging

Running `make run-debug` from the command line should immediately get you up and running with a local debug instance.
Then, from IntelliJ go to "edit configuration" and add a "Remote" target on port `5005`. This will attach the IDE's debugger to the running JVM.

## Making a change

Master should always have the version currently deployed. It is fine to commit directly to master, and make sure that the CD pipeline is always green, and that master is always deployed. Pull requests are mostly used for asking for feedback, comments and external contributions, and are not treated as a hard requirement to ship code. Deploy early and often.

We aim to review the PR's as soon as possible, but please give the team at least 2 working days to review your PR. If your PR is urgent, please ping us in #integrations. To speed up the review process we recommend keeping the PRs changes below 300 lines.

## E2E tests

If tests start to fail without any apparent changes, check whether the test user has its email address confirmed: https://sonar.soundcloud.org/users/user-434241656

If necessary ask [#comops](https://soundcloud.slack.com/archives/C025A5R3T) to confirm it.