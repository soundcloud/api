package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.web.BffInjectionBasedController
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.Urn.format
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class RateLimitingController(whitelistingService: WhitelistingService) extends BffInjectionBasedController {

  put("/-/ratelimits/whitelist/:client_urn") { request =>
    val clientUrn = Urn(request.routeParams("client_urn"))
    whitelistingService.whitelistClient(clientUrn) map { _ =>
      render.status(HttpResponseStatus.NO_CONTENT.getCode)
    }
  }

  delete("/-/ratelimits/whitelist/:client_urn") { request =>
    val clientUrn = Urn(request.routeParams("client_urn"))
    whitelistingService.unwhitelistClient(clientUrn) map { _ =>
      render.status(HttpResponseStatus.NO_CONTENT.getCode)
    }
  }

  get("/-/ratelimits/whitelist") { _ =>
    Future {
      render.ok.typedJson(whitelistingService.whitelistedClients)
    }
  }
}
