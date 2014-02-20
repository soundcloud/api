# Public-API Strangler

An implementation of the [Strangler Pattern](http://martinfowler.com/bliki/StranglerApplication.html) for the mothership's Public API

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
