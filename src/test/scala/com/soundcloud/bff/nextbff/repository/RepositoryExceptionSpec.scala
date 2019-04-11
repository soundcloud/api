package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsString, Json}

class RepositoryExceptionSpec extends UnitSpecification {
  val response = JsonResponseBuilder().status(Status.NotFound).body(Json.stringify(JsString("Not Found"))).build
  val exception = RepositoryException(response)

  "#apply" in {
    exception.status must be_==(Status.NotFound)
    exception.message must be_==("invalid response received: [status=404, body=\"Not Found\"]")
  }
}
