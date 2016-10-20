package com.soundcloud.publicApiStrangler.client.quota

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{Json, Reads}

class UserQuotaClient(jsonClient: JsonClient) {
  def downloadsPerTrack(session: UserSession, userUrns: Set[Urn]): Future[Map[Urn, Int]] = {
    jsonClient.get(session, Path() / "users" / "quotas", userUrns, Params.empty) map {
      case JsonResponse(OkStatus, body, _, _) => {
        body.as[List[DownloadsPerTrack]].map { entry =>
          entry.self.urn -> entry.downloads_per_track.getOrElse(Int.MaxValue)
        }.toMap
      }
      case _ => Map.empty[Urn, Int]
    } handle {
      case NonFatal(_) => Map.empty[Urn, Int]
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