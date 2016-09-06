package com.soundcloud.bff.nextbff.repository

import com.soundcloud.scalakit.finagle.http.NotFoundStatus
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import com.soundcloud.scalakit.test.UnitSpecification
import play.api.libs.json.JsString

class RepositoryExceptionSpec extends UnitSpecification {
  val jsonResponse = JsonResponse(NotFoundStatus, JsString("Not Found"))
  val exception = RepositoryException(jsonResponse)

  "#apply" in {
    exception.status must be_==(NotFoundStatus)
    exception.message must be_==("invalid response received: [status=404, json=\"Not Found\"]")
  }
}
