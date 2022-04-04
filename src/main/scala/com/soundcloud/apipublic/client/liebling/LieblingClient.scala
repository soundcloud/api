package com.soundcloud.apipublic.client.liebling

import com.soundcloud.apipublic.client.support.{FetchClient, ResponseHandlers}
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json, Reads, Writes}

import scala.util.control.NonFatal

/**
  * https://github.com/soundcloud/liebling/tree/master/doc
  */
class LieblingClient(jsonClient: JsonClient) extends FetchClient {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  def likeCounts(session: UserSession, targetUrns: Seq[Urn]): Future[List[LikesCount]] =
    inBatches(targetUrns.toList, 50) { urns =>
      fetchLikes(
        session,
        Path() / "likes_info",
        Map("for_urns" -> urns, "includes" -> "likes_counts")
      ).map(json => (json \ "likes_counts").as[List[LikesCount]])
    }.rescue {
      case NonFatal(ex) =>
        logger.error("error getting counts from Liebling", ex)
        Future.value(List.empty)
    }

  def userTotalLikeCount(session: UserSession, urns: Seq[Urn]): Future[List[UserTotalLikes]] = {
    fetchLikes(session, Path() / "users_counts", Map("for_urns" -> urns))
      .map(json => (json \ "users").as[List[UserTotalLikes]])
      .rescue {
        case NonFatal(ex) =>
          logger.error("error getting counts from Liebling", ex)
          Future.value(List.empty)
      }
  }

  private def fetchLikes(
      session: UserSession,
      path: Path,
      params: Params,
      headers: Headers = Headers.empty
  ): Future[JsObject] =
    fetch(
      jsonClient,
      session,
      path,
      params,
      headers
    ).map(ResponseHandlers.SingleItem(_))
}

case class LikesCount(target_urn: Urn, likes_count: Long)

object LikesCount {
  implicit val writes: Writes[LikesCount] = Json.writes[LikesCount]
  implicit val reads: Reads[LikesCount] = Json.reads[LikesCount]
}

case class UserTotalLikes(user_urn: Urn, track_likes_count: Long, playlist_likes_count: Long) {
  def totalLikeCount = track_likes_count + playlist_likes_count
}

object UserTotalLikes {
  implicit val writes: Writes[UserTotalLikes] = Json.writes[UserTotalLikes]
  implicit val reads: Reads[UserTotalLikes] = Json.reads[UserTotalLikes]
}
