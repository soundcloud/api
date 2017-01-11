package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http._
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.service.client.FetchClient
import com.twitter.util.{Future, NonFatal, Try}
import play.api.libs.json._

/**
  * https://github.com/soundcloud/voltron/tree/master/reposts
  */
class RepostsClient(jsonClient: JsonClient) extends FetchClient {

  def createRepost(session: UserSession, target: Urn, baseUrl: => String): Future[Result] =
    jsonClient.post(
      session,
      Path() / target.getCollection / target.toString / "reposts",
      Params.empty,
      Params.empty,
      None
    ).map(toResult(_, baseUrl))

  def deleteRepost(session: UserSession, target: Urn, baseUrl: => String): Future[Result] =
    jsonClient.delete(
      session,
      Path() / target.getCollection / target.toString / "reposts",
      Params.empty,
      Params.empty,
      None
    ).map(toResult(_, baseUrl))

  def getRepostCountsByUrnWithFallback(session: UserSession, urns: Set[Urn]): Future[Map[Urn, Long]] =
    repostCountsForUrns(session, urns)
      .map(_.map(count => count.urn -> count.count).toMap)
      .map { fetchedCounts =>
        urns.toList.map { urn => urn -> fetchedCounts.getOrElse(urn, 0L) }.toMap
      }

  private def repostCountsForUrns(session: UserSession, urns: Set[Urn]): Future[Set[Count]] =
    Future.collect(
      (urns.filter(_.getCollection == "users").map { userUrn =>
        getUserTotalCount(session, userUrn).map(Seq(_))
      } ++ Set(
        filterAndGetBulkCounts(session, "tracks", urns),
        filterAndGetBulkCounts(session, "playlists", urns)
      )).map(f => f.handle { case NonFatal(_) => Seq.empty }).toSeq
    ).map(_.flatten).map(_.toSet)

  private def filterAndGetBulkCounts(session: UserSession,
                                     collection: String,
                                     urns: Set[Urn],
                                     batchSize: Int = 50): Future[Seq[Count]] = {
    val filteredUrns = urns.filter(_.getCollection == collection)
    val path = Path() / collection / "reposts" / "count"

    Future.collect(
      filteredUrns.grouped(batchSize).map { batchUrns => {
        val params = Params("urns" -> batchUrns.map(_.toString).mkString(","))
        jsonClient.get(session, path, params, headers = Params.empty)
          .map { case JsonResponse(OkStatus, body, _, _) => (body \ "counts").as[Seq[Count]] }
      }}.map(f => f.handle { case NonFatal(_) => Seq.empty }).toSeq
    ).map(_.flatten)
  }

  private def getUserTotalCount(session: UserSession, user: Urn): Future[Count] =
    Future.join(
      getUserCountForKind(session, user, "track_reposts"),
      getUserCountForKind(session, user, "playlist_reposts")
    ).map { case (trackReposts, playlistReposts) =>
      Count(user, trackReposts.count + playlistReposts.count)
    }

  private def getUserCountForKind(session: UserSession, user: Urn, kind: String): Future[Count] =
    jsonClient.get(session, Path() / "users" / user.toString / kind / "count", Params.empty, Params.empty)
      .map { case JsonResponse(OkStatus, body, _, _) => (body \ "counts")(0).as[Count] }

}

object RepostsClient {

  trait Result
  case object Created extends Result
  case object Deleted extends Result
  case object AlreadyExists extends Result
  case object NotFound extends Result
  case class SpamWarning(warning_level: String, reason_phrase: String, acknowledge_url: Option[String], release_at: Option[String])
  case class SpamBlocked(errors: Seq[SpamWarning]) extends Result
  case object Failed extends Result

  case class Count(urn: Urn, count: Long)
  implicit val writesCount: Writes[Count] = Json.writes[Count]
  implicit val readsCount: Reads[Count] = Json.reads[Count]

  def toResult(response: JsonResponse, baseUrl: => String): Result = response match {
    case JsonResponse(CreatedStatus, _, _, _) => Created
    case JsonResponse(AcceptedStatus, _, _, _) => Deleted
    case JsonResponse(OkStatus, _, _, _) => AlreadyExists
    case JsonResponse(NotFoundStatus, _, _, _) => NotFound
    case JsonResponse(TooManyRequestsStatus, body, _, _) => SpamBlocked(toSpamWarnings(body, baseUrl))
    case _ => Failed
  }

  implicit val spamWarningFormat = Json.format[SpamWarning]
  implicit val spamBlockedFormat = Json.format[SpamBlocked]

  private def toSpamWarnings(body: JsValue, baseUrl: => String): Seq[SpamWarning] =
    (body \ "spam_warnings").as[JsArray].value.map { json =>
      val level = (json \ "level").as[String]
      val acknowledgeable = (json \ "acknowledgeable").as[Boolean]

      SpamWarning(
        warning_level = level,
        reason_phrase = s"$level: too many reposts",
        acknowledge_url = acknowledgeable match {
          case false => None
          case true => Some(s"$baseUrl/me/spam_warnings/${(json \ "id").as[Int]}/ack")
        },
        release_at = if (acknowledgeable) None else (json \ "release_at").asOpt[String]
      )
    }

}
