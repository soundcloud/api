package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Try
import play.api.libs.json.{Json, Reads}

object SimpleMapper {
  def apply[T](response: Response)(implicit fjs: Reads[T]): Option[T] =
    response.status match {
      case Status.Ok => Try(Json.parse(response.contentString).as[T]).toOption
      case _ => None
    }
}
