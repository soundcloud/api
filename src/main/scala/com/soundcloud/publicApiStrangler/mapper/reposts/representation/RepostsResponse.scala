package com.soundcloud.publicApiStrangler.mapper.reposts.representation

import play.api.libs.json._

case class RepostsResponse[T](collection: List[T], nextHref: Option[String])

object RepostsResponse {

  implicit def writes[T: Writes] = new Writes[RepostsResponse[T]] {
    override def writes(r: RepostsResponse[T]): JsValue =
      Json.obj("collection" -> r.collection.map(v => implicitly[Writes[T]].writes(v))) ++
        r.nextHref.map(nextHref => Json.obj("next_href" -> nextHref)).getOrElse(Json.obj())
  }
}
