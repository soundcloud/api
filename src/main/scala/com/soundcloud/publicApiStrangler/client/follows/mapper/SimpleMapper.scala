package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import com.twitter.util.Try
import play.api.libs.json.Reads

object SimpleMapper {

  def apply[T](response: JsonResponse)(implicit fjs: Reads[T]): Option[T] =
    response match {
      case JsonResponse(OkStatus, body, _, _) => Try(body.as[T]).toOption
      case _ => None
    }
}
