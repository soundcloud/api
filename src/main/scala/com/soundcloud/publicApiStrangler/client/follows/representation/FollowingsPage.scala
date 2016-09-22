package com.soundcloud.publicApiStrangler.client.follows.representation

import play.api.libs.json.Reads

case class FollowingsPage(followings: Seq[Following], next: Option[Pagination])

object FollowingsPage {

  implicit val reads: Reads[FollowingsPage] = Reads { json =>
    for {
      paginationLastId <- (json \ "page" \ "last_id").validate[Option[String]]
      pagination <- (json \ "page").validate[Option[Pagination]]
      followings <- (json \ "value").validate[Seq[Following]]
    } yield new FollowingsPage(followings, paginationLastId.flatMap(_ => pagination))
  }
}
