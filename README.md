# Public-API Strangler

An implementation of the [Strangler Pattern](http://martinfowler.com/bliki/StranglerApplication.html) for the mothership's Public API

## Team

* Team: Core Services <core-services@soundcloud.com>
* IRC: #coreservices. Slack: #core-services.
* Telemetry: [Promdash](http://promdash/public-api-strangler)
* Issue Tracker: [Jira Board](https://soundcloud.atlassian.net/secure/RapidBoard.jspa?rapidView=128)

## How to deploy

This project uses bucha as a deployment tool. In order to deploy the application, run:

```
bin/bucha api deploy
```

## Groups endpoint kill switch

The strangler has support for removing access to expensive endpoints that are harmful to our site-wide stability if abused or scrapped by a malicious user.

### How to use it

There are two features flags:

    1. disable_cheap_groups_endpoints ->  disables both  /users/:id/groups.json  and /groups/:id.json
    2. disable_expensive_groups_endpoints -> disables both /groups/:id/users.json and /groups/:id/users

Go to rollout and activate the features to the group `all`.

Activating it will make the strangler return HTTP 200 with empty body on requests to the corresponding.

### Links to rollout

http://rollout-web.int.s-cloud.net/activations/disable_cheap_groups_endpoints
http://rollout-web.int.s-cloud.net/activations/disable_expensive_groups_endpoints

Alternatively you can go to do http://rollout-web.int.s-cloud.net/features and click on the "On" button for the feature.

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
