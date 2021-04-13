package com.soundcloud.publicApiStrangler

import com.soundcloud.testutilities.{GratisMusikDiebstahl, SpinningUpAppSupport}
import org.specs2.mutable.Specification

class RateLimitingSanitySpecification extends Specification with SpinningUpAppSupport {

  trait RateContext extends Context {
    val server = TestServer("publicapistrangler", 5000)
    val adminServer = TestServer("publicapistrangler", 5001)

    ZKSetup.setData("/public-api-strangler/rollouts/wire-rate-limits", "100")
    ZKSetup.setData("/public-api-strangler/rollouts/probe-rate-limits", "100")
    ZKSetup.setData("/public-api-strangler/rollouts/enforce-rate-limits", "100")
    ZKSetup.setData(
      "/ratelimiting/public-api-strangler/ratelimitgroups/default",
      """
        |{
        |  "id": "default",
        |  "rate_limits": [
        |    {
        |      "name": "limit-foo",
        |      "matching": "/foo",
        |      "configurations": [
        |        {
        |          "bucket": "by-client",
        |          "time_window": "PT1M",
        |          "max_nr_of_requests": 1000
        |        }
        |      ],
        |      "mode": "probing"
        |    },
        |    {
        |      "name": "search",
        |      "matching": "",
        |      "configurations": [
        |        {
        |          "bucket": "by-client",
        |          "time_window": "PT1M",
        |          "max_nr_of_requests": 3
        |        }
        |      ],
        |      "mode": "enforcing"
        |    },
        |    {
        |      "name": "plays",
        |      "matching": "",
        |      "configurations": [
        |        {
        |          "bucket": "by-client",
        |          "time_window": "PT1M",
        |          "max_nr_of_requests": 3
        |        }
        |      ],
        |      "mode": "enforcing"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  "Public API Strangler" should {

    "rate limit test requests satisfying custom classifier, with search query params" in new RateContext {

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&q=search-foo").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&license=search-bar").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&tags=search-baz").status ==== 200

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&q=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&license=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&tags=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&genres=search-foo").status ==== 429

      server.get(s"/search?q=foo&client_id=${GratisMusikDiebstahl.clientId}").status ==== 429
      server.get(s"/v1/tracks?q=foo&client_id=${GratisMusikDiebstahl.clientId}").status ==== 429

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&foo=search-foo").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
    }

    "ratelimit requests for track streams injected via config in code, and not ZK" in new RateContext {
      server.get(s"/i1/tracks/177748926/streams?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
      server.get(s"/i1/tracks/177748926/streams?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
      server.get(s"/i1/tracks/177748926/streams?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200

      server.get(s"/i1/tracks/177748926/streams?client_id=${GratisMusikDiebstahl.clientId}").status ==== 429

    }
  }
}
