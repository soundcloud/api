package com.soundcloud.publicApiStrangler.mapping.reposts

import play.api.libs.json._
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser.{writes => repostsUserWrites}

case class RepostsResponse[T](collection: List[T], nextHref: Option[String])

object RepostsResponse {
  private def writes[T](implicit tWrites: Writes[T]) = new Writes[RepostsResponse[T]] {
    override def writes(r: RepostsResponse[T]): JsValue =
      Json.obj("collection" -> r.collection.map(v => tWrites.writes(v))) ++
        r.nextHref.map(nextHref => Json.obj("next_href" -> nextHref)).getOrElse(Json.obj())
  }

  implicit val repostsUserResponseWrites = writes[RepostsUser]
  implicit val repostablesResponseWrites = writes[Long]
}
