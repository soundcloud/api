package com.soundcloud.publicApiStrangler.mapping.reposts

import play.api.libs.json._
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser.writes

case class Reposters(collection: List[RepostsUser], nextHref: Option[String])

object Reposters {
  implicit val writes = new Writes[Reposters] {
    override def writes(r: Reposters): JsValue =
      Json.obj("collection" -> r.collection
                 .map(v => RepostsUser.writes.writes(v))) ++
        r.nextHref.map(next => Json.obj("next_href" -> next)).getOrElse(Json.obj())
  }
}
