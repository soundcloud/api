package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{Format, JsObject, Json}

import scala.util.control.NonFatal

case class Reposts(urns: List[Urn], nextCursor: Option[String])

/**
  * https://github.com/soundcloud/voltron/tree/master/reposts
  */
class RepostsClient(jsonClient: JsonClient) extends FetchClient {
  def createRepost(session: UserSession, target: Urn): Future[Result] =
    jsonClient
      .postWithSession(
        session,
        Path() / target.collection / target.toString / "reposts",
        Params.empty,
        Headers.empty,
        None
      )
      .map(toResult)

  def deleteRepost(session: UserSession, target: Urn): Future[Result] =
    jsonClient
      .deleteWithSession(
        session,
        Path() / target.collection / target.toString / "reposts",
        Params.empty,
        Headers.empty,
        None
      )
      .map(toResult)

  def getRepostCountsByUrnWithFallback(session: UserSession, urns: Set[Urn]): Future[Map[Urn, Long]] =
    repostCountsForUrns(session, urns)
      .map(_.map(count => count.urn -> count.count).toMap)
      .map { fetchedCounts =>
        urns.toList.map { urn =>
          urn -> fetchedCounts.getOrElse(urn, 0L)
        }.toMap
      }

  def reposters(session: UserSession, repostableUrn: Urn, limit: Int, maybeCursor: Option[String]): Future[Reposts] =
    fetchAll(
      session,
      Path() / repostableUrn.collection / repostableUrn / "reposts",
      limit,
      maybeCursor
    ).map {
      case (repostsJson, cursor) =>
        val reposts = repostsJson.map(repost => (repost \ "user").as[Urn])
        Reposts(reposts, cursor)
    }

  def trackReposts(session: UserSession, user: Urn, limit: Int, maybeCursor: Option[String]): Future[Reposts] =
    reposts(session, user, "track", limit, maybeCursor)

  def playlistReposts(session: UserSession, user: Urn, limit: Int, maybeCursor: Option[String]): Future[Reposts] =
    reposts(session, user, "playlist", limit, maybeCursor)

  private def reposts(
      session: UserSession,
      user: Urn,
      kind: String,
      limit: Int,
      maybeCursor: Option[String]
  ): Future[Reposts] =
    fetchAll(
      session,
      Path() / "users" / user / s"${kind}_reposts",
      limit,
      maybeCursor
    ).map {
      case (repostsJson, cursor) =>
        val reposts = repostsJson.map(repost => (repost \ "repostable").as[Urn])
        Reposts(reposts, cursor)
    }

  private def fetchAll(
      session: UserSession,
      path: Path,
      limit: Int,
      cursor: Option[String]
  ): Future[(List[JsObject], Option[String])] = {
    jsonClient
      .getWithSession(
        session,
        path,
        Params("page_size" -> limit) ++ cursor.map(c => Params("cursor" -> c)).getOrElse(Params.empty),
        Headers.empty
      )
      .map { response: Response =>
        response.status match {
          case Status.Ok =>
            val body = Json.parse(response.contentString)
            val reposts = (body \ "reposts").as[List[JsObject]]
            val maybeNewCursor = (body \ "next" \ "cursor").asOpt[String]
            (reposts, maybeNewCursor)
          case _ => (List.empty, None)
        }
      }
  }

  private def repostCountsForUrns(session: UserSession, urns: Set[Urn]): Future[Set[Count]] =
    Future
      .collect(
        Set(
          getBulkUserRepostCounts(session, urns.filter(_.collection == "users")),
          filterAndGetBulkCounts(session, "tracks", urns),
          filterAndGetBulkCounts(session, "playlists", urns)
        ).map(f => f.handle { case NonFatal(_) => Seq.empty }).toSeq
      )
      .map(_.flatten)
      .map(_.toSet)

  private def filterAndGetBulkCounts(
      session: UserSession,
      collection: String,
      urns: Set[Urn],
      batchSize: Int = 100
  ): Future[Seq[Count]] = {
    val filteredUrns = urns.filter(_.collection == collection)
    val path = Path() / collection / "reposts" / "count"

    Future
      .collect(
        filteredUrns
          .grouped(batchSize)
          .map { batchUrns =>
            {
              val params = Params("urns" -> batchUrns.map(_.toString).mkString(","))
              jsonClient
                .getWithSession(session, path, params, headers = Headers.empty)
                .map {
                  case response if response.status == Status.Ok =>
                    (Json.parse(response.contentString) \ "counts").as[Seq[Count]]
                }
            }
          }
          .map(f => f.handle { case NonFatal(_) => Seq.empty })
          .toSeq
      )
      .map(_.flatten)
  }

  def getBulkUserRepostCounts(session: UserSession, users: Set[Urn], batchSize: Int = 100): Future[Seq[Count]] =
    Future
      .collect(
        groupByLimit(users.toSeq, batchSize).map(batch => userCountByBatch(batch.toSet, session))
      )
      .map(_.flatten)

  private[reposts] def groupByLimit(users: Seq[Urn], batchSize: Int = 100): Seq[Seq[Urn]] =
    users
      .grouped(if (Math.abs(batchSize) > 100 || batchSize == 0) 100 else Math.abs(batchSize))
      .toSeq

  private def userCountByBatch(users: Set[Urn], session: UserSession): Future[Seq[Count]] =
    Future
      .join(
        getBatchUserCountForKind(session, users, "track_reposts"),
        getBatchUserCountForKind(session, users, "playlist_reposts")
      )
      .map(batchCount => (batchCount._1 ++ batchCount._2).groupBy(_.urn))
      .map { m =>
        m.collect {
          case (urn, counts) => Count(urn, counts.map(_.count).sum)
        }
      }
      .map(_.toSeq)

  private def getBatchUserCountForKind(session: UserSession, batchUrns: Set[Urn], kind: String): Future[Seq[Count]] =
    jsonClient
      .getWithSession(
        session,
        Path() / "users" / kind / "count",
        Params("urns" -> batchUrns.map(_.toString).mkString(",")),
        Headers.empty
      )
      .map {
        case response if response.status == Status.Ok => (Json.parse(response.contentString) \ "counts").as[Seq[Count]]
        case _ => Seq.empty
      }
}

object RepostsClient {
  sealed trait Result

  case object Created extends Result

  case object Deleted extends Result

  case object AlreadyExists extends Result

  case object NotFound extends Result

  case object SpamBlocked extends Result

  case object Failed extends Result

  case object Forbidden extends Result

  case class Count(urn: Urn, count: Long)

  implicit val countFormat: Format[Count] = Json.format[Count]

  def toResult(response: Response): Result = response.status match {
    case Status.Created => Created
    case Status.Accepted => Deleted
    case Status.Ok => AlreadyExists
    case Status.NotFound => NotFound
    case Status.TooManyRequests => SpamBlocked
    case _ => Failed
  }
}
