# Public-API Strangler

An implementation of the [Strangler Pattern](http://martinfowler.com/bliki/StranglerApplication.html) for the mothership's Public API.

## Team

* Team: Core Services <core-services@soundcloud.com>
* IRC: #coreservices. Slack: #core-services.
* Telemetry: [Promdash](http://promdash/public-api-strangler)
* Issue Tracker: [Jira Board](https://soundcloud.atlassian.net/secure/RapidBoard.jspa?rapidView=128).

## How to develop locally

Take a look in [CONTRIBUTING.md](CONTRIBUTING.md). This application uses docker-compose to orchestrate its dependencies. In order to run the application locally, please make sure to have docker and docker-compose installed. Before commiting, make sure to run `make precheckin` and that the tests are passing

## How to deploy

Use jenkins to deploy the application. For the master branch and deployment pipeline: [http://jenkins.cs.dev.s-cloud.net/view/public-api-strangler/](http://jenkins.cs.dev.s-cloud.net)


## CI
For the master branch and deployment pipeline: [http://jenkins.cs.dev.s-cloud.net/view/public-api-strangler/](http://jenkins.cs.dev.s-cloud.net/view/public-api-strangler/).

The PR precheckin builds are still on jenkins.int due the laufbursche integration present there: [http://jenkins.int.s-cloud.net/job/public-api-strangler_master_precheckin](http://jenkins.int.s-cloud.net/job/public-api-strangler_master_precheckin).


## Groups endpoint kill switch

The strangler has support for removing access to expensive endpoints that are harmful to our site-wide stability if abused or scrapped by a malicious user.

### How to use it

There are two features flags:

    1. disable_cheap_groups_endpoints ->  disables both  /users/:id/groups.json  and /groups/:id.json
    2. disable_expensive_groups_endpoints -> disables both /groups/:id/users.json and /groups/:id/users

Go to rollout and activate the features to the group `all`.

Activating it will make the strangler return HTTP 200 with empty body on requests to the corresponding.

### Links to rollout

http://gatekeeper.int.s-cloud.net/activations/disable_cheap_groups_endpoints
http://gatekeeper.int.s-cloud.net/activations/disable_expensive_groups_endpoints

Alternatively you can go to do http://gatekeeper.int.s-cloud.net/features and click on the "On" button for the feature.

## Search leaving mothership

Routing of search requests is controlled via rollout flags.

1. [`search_avoid_mothership_for_tracks`](http://gatekeeper.int.s-cloud.net/activations/search_avoid_mothership_for_tracks)
   -> uses microservices for /tracks?q= instead of mothership
1. [`search_avoid_mothership_for_users`](http://gatekeeper.int.s-cloud.net/activations/search_avoid_mothership_for_users)
   -> uses microservices for /users?q= instead of mothership
1. [`search_avoid_mothership_for_playlists`](http://gatekeeper.int.s-cloud.net/activations/search_avoid_mothership_for_playlists)
   -> uses microservices for /playlists?q= instead of mothership
1. [`search_avoid_mothership_for_groups`](http://gatekeeper.int.s-cloud.net/activations/search_avoid_mothership_for_groups)
   -> uses microservices for /groups?q= instead of mothership

## Rate limiting whitelist
The ratelimiting feature makes use of a whitelist of client application URNs that will never be rate-limited. The source of truth for this whitelist is the ZooKeeper cluster.

In the near future, it will be possible to add clients to the whitelist or remove them by means of of a corresponding internal service that will expose appropriate resources over HTTP.

Until that service is in place, updating the whitelist has to be done with a simple Python script located in this repository, which will remove the previous whitelist from ZooKeeper and replace it with the whitelist in `ratelimiting_whitelist.txt`. This at least allows us to quickly add clients to the whitelist without requiring a deployment.

### Requirements
Make sure you have Python and the [zk-shell](https://github.com/rgs1/zk_shell) command-line utility installed on your machine:

`pip install zk-shell`

### How to update the whitelist

If you want to replace the current version of the whitelist with a new one, do this:

1. Change the file `ratelimiting_whitelist.txt` to contain exactly those client app URNs that should be on the whitelist. Each URN must be in a separate line.
2. Run `./update_ratelimiting_whitelist.py -u ratelimiting_whitelist.txt`. The script will print out what it's doing, indicating a successful run with a `Done!` at the end.

Adding or removing an individual client is a lot faster and should be preferred:

```
./update_ratelimit_whitelist.py -a 123242
./update_ratelimit_whitelist.py -r 123243
```

If you want to do a dry run, testing your update against a local ZooKeeper server, replace the line `zookeeper_server = zookeeper_host()` with `zookeeper_server = 'localhost'`.


## FAQ

### What should I read before asking questions?
That's a great question! Try these first:

* [Strangler Application, by Martin Fowler](http://martinfowler.com/bliki/StranglerApplication.html)
* [Legacy Application Strangulation : Case Studies, by Paul Hammant](http://paulhammant.com/2013/07/14/legacy-application-strangulation-case-studies/)
* [An Introduction to Finagle](http://twitter.github.io/scala_school/finagle.html)

### Is this going to replace the current public api?
Yes and no. This will be the first service hit when someone invokes
`api.soundcloud.com`, but it doesn't aim to replace the [Mothership](http://github.com/soundcloud/soundcloud).

The Mothership and its ecosystem implement a lot of different concerns
and
[Bounded Contexts](http://martinfowler.com/bliki/BoundedContext.html).
One of the concerns is to provide a gateway from the internet to
internal services, like search, payments, uploads and streamming.
While there is no plan to port them to this service immediately, new
services and huge enough refactorings of old services should consider
this system, and not the mothership, as its main way out to the
internet.

### Will this become the new public API?
No. If you are interested in what is coming for the API, please reach
out to the platform team:
[platform@soundcloud.com](platform@soundcloud.com). This is just a
smart proxy to the old API.

### Besides enablign the Strangler Pattern, what are the benefits of this layer?
Several small features, like rate-limiting and other security checks,
are implemented at this layer, without having to touch the mothership
code.
