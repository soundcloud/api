package com.soundcloud.bff.nextbff.repository

import com.soundcloud.publicApiStrangler.client.JsonResponse
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.Successful
import play.api.libs.json.{JsArray, JsObject}

trait SafeJsonHandler {

  protected def toJsonObject(response: Response) =
    JsonResponse.from(response) match {
      case JsonResponse(Successful(_), Right(json: JsObject), _) => json
      case _ => throw RepositoryException(response)
    }

  protected def toJsonArray(response: Response) =
    JsonResponse.from(response) match {
      case JsonResponse(Successful(_), Right(json: JsArray), _) => json
      case _ => throw RepositoryException(response)
    }
}
