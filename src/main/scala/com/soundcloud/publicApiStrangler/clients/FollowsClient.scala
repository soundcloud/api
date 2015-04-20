package com.soundcloud.publicApiStrangler.clients

import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.finagle.http.{PreconditionFailedStatus, CreatedStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json.{JsString, Json, JsValue, JsObject}


class FollowsClient(jsonService: JsonService) {

  def follow(userSession: UserSession, target: Urn): Future[FollowResponse] = {
    jsonService.post(userSession,
      Path() / "follow" / target.getString,
      Params.empty,
      None
    ).map {
      case JsonResponse(CreatedStatus, _, _, _) => FollowSuccessful(target)
      case JsonResponse(PreconditionFailedStatus, data, _, _) => FollowFailed(data)
    }
  }

  def mutualFollowers(userSession: UserSession, user: Urn, otherUser: Urn, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / user.getString / "mutual_followers" / otherUser.getString,
      pageSize,
      lastId
    )
  }

  def followingsNotFollowedBy(userSession: UserSession, user: Urn, otherUser: Urn, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / user.getString / "followings_not_followed" / otherUser.getString,
      pageSize,
      lastId
    )
  }

  def mutualFollowings(userSession: UserSession, user: Urn, otherUser: Urn, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / user.getString / "mutual_followings" / otherUser.getString,
      pageSize,
      lastId
    )
  }

  def followers(userSession: UserSession, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / userSession.getUser.getString / "followers",
      pageSize,
      lastId
    )
  }

  def followings(userSession: UserSession, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    fetchPage(
      userSession,
      Path() / "users" / userSession.getUser.getString / "followings",
      pageSize,
      lastId
    )
  }

  private def fetchPage(userSession: UserSession, path: Path, pageSize: Int, lastId: Option[String]): Future[FollowsPage] = {
    val params = Map("page_size" -> pageSize.toString) ++ lastId.map("last_id" -> _)

    jsonService.get(userSession,
      path,
      params
    ).map {
      case JsonResponse(OkStatus, data, _, _) =>
        val values = (data \ "value").as[Seq[JsObject]].map { value =>
          Following(
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

case class Following(id: String, created: String, target: Urn, user: Urn)
case class PageInfo(lastId: Option[String], size: Int)
case class FollowsPage(values: Seq[Following], page: PageInfo)

trait FollowResponse
case class FollowSuccessful(target: Urn) extends FollowResponse
case class FollowFailed(response: JsValue) extends FollowResponse {

  def status = (response \ "error" \ "status").as[Int]

  def message: String = (response \ "error" \ "message").asOpt[String].getOrElse(name)

  def name = (response \ "error" \ "name").as[String]

  def isAgeRestricted = name == "AgeRestrictedException"

  def isAgeUnknown = name == "AgeUnknownException"

}
