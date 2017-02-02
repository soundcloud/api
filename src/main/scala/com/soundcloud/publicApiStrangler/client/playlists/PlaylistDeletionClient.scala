package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.experimental.result._
import com.soundcloud.jvmkit.module.httpclient.{Headers, HttpClient, HttpResponse, HttpStatus, Params}
import com.soundcloud.jvmkit.module.servicediscovery.Path
import com.soundcloud.jvmkit.module.util.{Urn, UserSession}
import com.twitter.util.{Future, NonFatal}

class PlaylistDeletionClient(jsonClient: HttpClient) {
  def deletePlaylist(session: UserSession, urn: Urn): Future[Result[HttpStatus]] = {
    jsonClient.deleteWithSession(
      session,
      Path() / "playlists" / urn,
      Params.empty,
      Headers.empty,
      None
    ).map {
      case HttpResponse(status, _, _) => Good(status)
    } handle { case NonFatal(e) => Bad(Error("Unhandled exception when deleting playlist.", e)) }
  }
}

object PlaylistDeletionClient {
  case class HttpError(status: HttpStatus) extends ErrorLike
}
