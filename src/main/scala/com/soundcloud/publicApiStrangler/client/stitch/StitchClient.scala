package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn.format
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future

class StitchClient(jsonClient: JsonClient) {
  def countsForTrack(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[StitchCounts] =
    for {
      playbackCount <- playbackCountForTrack(session, trackUrn, userUrn)
      downloadCount <- downloadCountForTrack(session, trackUrn, userUrn)
      favoritingsCount <- favoritingsCountForTrack(session, trackUrn, userUrn)
      commentsCount <- commentsCountForTrack(session, trackUrn, userUrn)
    } yield {
      StitchCounts(
        playback_count = playbackCount,
        download_count = downloadCount,
        favoritings_count = favoritingsCount,
        comment_count = commentsCount
      )
    }

  private def playbackCountForTrack = countForTrack("p.o.t") _
  private def downloadCountForTrack = countForTrack("d.o.t") _
  private def favoritingsCountForTrack = countForTrack("l.o.t") _
  private def commentsCountForTrack = countForTrack("c.o.t") _

  private def countForTrack(cat: String)(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[Int] = {
    val key = s"${userUrn.getIdentifier}|${trackUrn.getIdentifier}"
    val params = Params("c" -> cat, "keys" -> key, "r" -> "a")
    jsonClient.get(session, Path() / "ts", params).map {
      case JsonResponse(OkStatus, body, _, _) => ((body \ key \ "series")(0) \ "count").as[Int]
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }
}
