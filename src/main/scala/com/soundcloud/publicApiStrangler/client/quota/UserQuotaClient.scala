package com.soundcloud.publicApiStrangler.client.quota

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.CommonJsonFormats.urnFormat
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{Json, Reads}

import scala.util.control.NonFatal

class UserQuotaClient(jsonClient: JsonClient) {
  def downloadsPerTrack(session: UserSession, userUrns: Set[Urn]): Future[Map[Urn, Option[Int]]] = {
    jsonClient.getWithSession(session, Path() / "users" / "quotas", userUrns, Headers.empty()) map {
      response: Response =>
        response.status match {
          case Status.Ok => {
            Json.parse(response.contentString).as[List[DownloadsPerTrack]].map { entry =>
              entry.self.urn -> entry.downloads_per_track
            }.toMap
          }
          case _ => Map.empty[Urn, Option[Int]]
        }
    } handle {
      case NonFatal(_) => Map.empty[Urn, Option[Int]]
    }
  }
}

case class Self(urn: Urn)

case class DownloadsPerTrack(downloads_per_track: Option[Int], self: Self)

object Self {
  implicit val reads: Reads[Self] = Json.reads[Self]
}

object DownloadsPerTrack {
  implicit val reads: Reads[DownloadsPerTrack] = Json.reads[DownloadsPerTrack]
}