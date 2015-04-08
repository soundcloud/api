package com.soundcloud.publicApiStrangler.clients

import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json.JsObject


class FollowsClient(jsonService: JsonService) {

  def followersFollowedBy(userSession: UserSession, user: Urn, otherUser: Urn, pageSize: Int): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / user.getString / "followers_followers_by" / otherUser.getString,
      pageSize
    )
  }

  def followingsNotFollowedBy(userSession: UserSession, user: Urn, otherUser: Urn, pageSize: Int): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / user.getString / "followings_not_followed" / otherUser.getString,
      pageSize
    )
  }

  def followers(userSession: UserSession, pageSize: Int): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / userSession.getUser.getString / "followers",
      pageSize
    )
  }

  def followings(userSession: UserSession, pageSize: Int): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / userSession.getUser.getString / "followings",
      pageSize
    )
  }

  private def fetchPage(userSession: UserSession, path: Path, pageSize: Int): Future[FollowsPage] = {
    jsonService.get(userSession,
      path,
      Map("page_size" -> pageSize)
    ).map {
      case JsonResponse(OkStatus, data, _, _) =>
        val values = (data \ "value").as[Seq[JsObject]].map { value =>
          Affiliation(
            (value \ "id").as[String],
            (value \ "created").as[String],
            Urn((value \ "target").as[String]),
            Urn((value \ "user").as[String])
          )
        }

        val pageInfo = PageInfo(
          (data \ "page" \ "last_id").asOpt[String],
          (data \ "page" \ "size").as[Int]
        )

        FollowsPage(values, pageInfo)
      case _ =>
        throw new IllegalArgumentException
    }
  }
}

case class Affiliation(id: String, created: String, target: Urn, user: Urn)
case class PageInfo(lastId: Option[String], size: Int)
case class FollowsPage(values: Seq[Affiliation], page: PageInfo)