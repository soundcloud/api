package com.soundcloud.publicApiStrangler.client.follows.representation

import play.api.libs.json.{JsSuccess, Reads}

case class FollowingsPage(followings: Seq[Following], next: Option[Pagination])

object FollowingsPage {
  implicit val reads: Reads[FollowingsPage] = Reads { json =>
    for {
      paginationLastId <- JsSuccess((json \ "page" \ "last_id").asOpt[String])
      pagination <- JsSuccess((json \ "page").asOpt[Pagination])
      followings <- (json \ "value").validate[Seq[Following]]
    } yield new FollowingsPage(followings, paginationLastId.flatMap(_ => pagination))
  }
}
