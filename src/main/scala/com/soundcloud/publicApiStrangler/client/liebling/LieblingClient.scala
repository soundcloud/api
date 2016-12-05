package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.jvmkit.Urn.format
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.jsonservice.{Params, _}
import com.soundcloud.service.client.{FetchClient, ResponseHandlers}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{JsObject, Json, Reads, Writes}

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

  def userLikeCounts(session: UserSession, targetUrns: Seq[Urn], user: Urn, batchSize: Int = 25): Future[UserLikesCount] = {
    val fetch: (Seq[Urn]) => Future[UserLikesCount] = batch => fetchLikes(
      session,
      Path() / "likes_info",
      Map("for_urns" -> batch, "includes" -> "likes_counts,liked_track_urns", "user_urn" -> user)
    ).map(_.as[UserLikesCount])
    val combine: (UserLikesCount, UserLikesCount) => UserLikesCount = {
      case (ulc1, ulc2) =>
        UserLikesCount(
          ulc1.liked_track_urns ++ ulc2.liked_track_urns,
          ulc1.likes_counts ++ ulc2.likes_counts
        )
    }
    batched(targetUrns, batchSize)(fetch)(combine)
  }

  private def fetchLikes(session: UserSession, path: Path, params: Params, headers: Params = Params.empty): Future[JsObject] =
    fetch(
      jsonClient,
      session,
      path,
      params,
      headers
    ).map(ResponseHandlers.SingleItem(_))

  private def batched[I, O](inputs: Seq[I], batchSize: Int)
                           (fetch: (Seq[I]) => Future[O])
                           (combine: (O, O) => O): Future[O] =
    Future.collect {
      inputs.grouped(batchSize).map(fetch).toSeq
    }.map(_.reduce(combine))
}

case class LikesCount(target_urn: Urn, likes_count: Long)

object LikesCount {
  implicit val writes: Writes[LikesCount] = Json.writes[LikesCount]
  implicit val reads: Reads[LikesCount] = Json.reads[LikesCount]
}

case class UserLikesCount(liked_track_urns: Set[Urn], likes_counts: List[LikesCount])

object UserLikesCount {
  implicit val writes: Writes[UserLikesCount] = Json.writes[UserLikesCount]
  implicit val reads: Reads[UserLikesCount] = Json.reads[UserLikesCount]
}
