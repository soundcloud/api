package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.publicApiStrangler.client.support.{FetchClient, ResponseHandlers}
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json, Reads, Writes}

import scala.util.control.NonFatal

/**
  * https://github.com/soundcloud/liebling/tree/master/doc
  */
class LieblingClient(jsonClient: JsonClient, exceptionCollector: ExceptionCollector) extends FetchClient {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  /**
    * @see https://github.com/soundcloud/liebling/tree/master/doc#like-a-track
    */
  def createTrackLike(session: UserSession, track: Urn): Future[CreateLikeResponse] =
    createLike(
      session,
      Path() / "tracks" / track.toString / "likes"
    )

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

  def userLikeCounts(
      session: UserSession,
      targetUrns: Seq[Urn],
      user: Urn,
      batchSize: Int = 25
  ): Future[UserLikesCount] = {
    val fetch: (Seq[Urn]) => Future[UserLikesCount] = batch =>
      fetchLikes(
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

  /**
    * Returns a map of track urn to boolean indicating if the provided user has liked that track or not.
    */
  def userLikedTracks(
      session: UserSession,
      trackUrns: Set[Urn],
      user: Urn,
      batchSize: Int = 25
  ): Future[Map[Urn, Boolean]] = {
    // initialize all track urns as not liked
    val defaultLikes = trackUrns.map((_ -> false)).toMap
    userLikeCounts(session, trackUrns.toSeq, user, batchSize)
      .map(userLikeCounts => {
        userLikeCounts.liked_track_urns.map((_ -> true)).toMap
      })
      .map(defaultLikes ++ _)
  }

  /**
    * Returns the tracks from given urn input list that are liked by given user
    *
    * @param session   User session.
    * @param userUrn   User urn.
    * @param trackUrns Input track urns.
    * @return List of track urns from input list that are liked by given user as defined by user urn.
    * @see https://github.com/soundcloud/liebling/tree/master/doc#user-content-get-likes-info
    */
  def userTracksLikesForUrns(session: UserSession, userUrn: Urn, trackUrns: List[Urn]): Future[List[Urn]] =
    inBatches(trackUrns, 50) { urnBatch =>
      fetchLikes(
        session,
        Path() / "likes_info",
        Map("for_urns" -> urnBatch, "user_urn" -> userUrn, "includes" -> "liked_track_urns")
      ).map(json => (json \ "liked_track_urns").as[List[Urn]])
    }.handle {
      case NonFatal(_) => List.empty
    }

  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      cursor: Option[String],
      pageSize: Int = 50
  ): Future[LikesPage] = {
    fetch(jsonClient, session, Path() / "users" / userUrn / "track_likes", paramsFor(cursor, pageSize))
      .map { response =>
        Json.parse(response.contentString).as[LikesPage]
      }
      .handleAndReport(exceptionCollector) {
        case NonFatal(_) => emptyLikesPage
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

  private def batched[I, O](inputs: Seq[I], batchSize: Int)(
      fetch: (Seq[I]) => Future[O]
  )(combine: (O, O) => O): Future[O] =
    Future
      .collect {
        inputs.grouped(batchSize).map(fetch).toSeq
      }
      .map(_.reduce(combine))

  private def paramsFor(cursor: Option[String], pageSize: Int, urns: List[Urn] = List.empty): Params =
    Map("page_size" -> pageSize.toString) ++
      cursor.map(c => Map("cursor" -> c)).getOrElse(Map.empty) ++
      urns.headOption.map(_ => Map("urns" -> urns.map(_.toString).mkString(","))).getOrElse(Map.empty)

  private def createLike(session: UserSession, path: Path): Future[CreateLikeResponse] =
    jsonClient
      .postWithSession(
        session,
        path,
        Params.empty,
        Headers.empty,
        jsonBody(session)
      )
      .map(CreateLikeResponseMapper(_))

  private def jsonBody(session: UserSession): Option[String] = {
    Some(Json.obj("user_urn" -> session.getUser.toString).toString)
  }

  def emptyLikesPage: LikesPage = LikesPage(
    likes = List.empty,
    meta = LikesPageMeta(
      cursor = LikesPageCursor(
        next_params = None,
        next_href = None
      )
    )
  )
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

case class UserTotalLikes(user_urn: Urn, track_likes_count: Long, playlist_likes_count: Long) {
  def totalLikeCount = track_likes_count + playlist_likes_count
}

object UserTotalLikes {
  implicit val writes: Writes[UserTotalLikes] = Json.writes[UserTotalLikes]
  implicit val reads: Reads[UserTotalLikes] = Json.reads[UserTotalLikes]
}
