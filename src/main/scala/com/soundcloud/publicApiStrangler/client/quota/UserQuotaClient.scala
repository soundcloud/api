package com.soundcloud.publicApiStrangler.client.quota

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats.urnFormat
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.Future
import play.api.libs.json.{Json, Reads}

import scala.util.control.NonFatal

class UserQuotaClient(jsonClient: JsonClient) {
  def downloadsPerTrack(session: UserSession, userUrns: Set[Urn]): Future[Map[Urn, Option[Int]]] = {
    jsonClient.get(session, Path() / "users" / "quotas", toBigJvmKitUrnSet(userUrns), Params.empty) map {
      case JsonResponse(OkStatus, body, _, _) => {
        body.as[List[DownloadsPerTrack]].map { entry =>
          entry.self.urn -> entry.downloads_per_track
        }.toMap
      }
      case _ => Map.empty[Urn, Option[Int]]
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