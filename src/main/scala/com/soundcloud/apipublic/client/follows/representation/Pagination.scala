package com.soundcloud.apipublic.client.follows.representation

import play.api.libs.json.Reads

case class Pagination(cursor: String, page_size: Int)

object Pagination {
  implicit val reads: Reads[Pagination] = Reads { json =>
    for {
      cursor <- (json \ "last_id").validate[String]
      size <- (json \ "size").validate[Int]
    } yield new Pagination(cursor, size)
  }
}
