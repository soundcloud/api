package com.soundcloud.publicApiStrangler.clients

import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future


class FollowsClient(jsonService: JsonService) {

  def followings(userSession: UserSession, pageSize: Int): Future[FollowsPage] = {
    jsonService.get(userSession,
      Path() / "users" / userSession.getUser / "followings",
      Map("page_size" -> pageSize)
    ).map {
      case JsonResponse(OkStatus, data, _, _) =>
        val values = (data \\ "value").map { value =>
          Affiliation(
            (value \ "id").as[String],
            (value \ "created").as[String],
            Urn((value \ "target").as[String]),
            Urn((value \ "user").as[String])
          )
        }
        FollowsPage(values)
      case _ =>
        throw new IllegalArgumentException
    }
  }

}

case class Affiliation(id: String, created: String, target: Urn, user: Urn)
case class FollowsPage(values: Seq[Affiliation])