package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.{JsArray, JsObject}
import com.soundcloud.scalakit.finagle.http.SuccessfulStatusClass
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse

trait SafeJsonHandler {

  protected def toJsonObject(response: JsonResponse) =
    response match {
      case JsonResponse(SuccessfulStatusClass(code), json: JsObject, _, _) => json
      case response => throw RepositoryException(response)
    }

  protected def toJsonArray(response: JsonResponse) =
    response match {
      case JsonResponse(SuccessfulStatusClass(code), json: JsArray, _, _) => json
      case response => throw RepositoryException(response)
    }
}
