package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, ListParam, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Client for MediaUrlgen.
  *
  * @param jsonClient        Json client.
  * @param trackStreamMapper   TrackStreamMapper
  */
private[media] class MediaUrlgenClient(jsonClient: JsonClient, trackStreamMapper: TrackStreamUrlMapper) {
  /**
    * Client for MediaUrlgen.
    *
    * @param jsonClient Json client.
    */
  def this(jsonClient: JsonClient) {
    this(jsonClient, new TrackStreamUrlMapper)
  }

  /**
    * Gets available track stream urls for given user session and track.
    *
    * @param session              User session.
    * @param trackUrn             Track Urn.
    * @param requestProperties    User supplied request properties. Should contains 'ssl' -> true/false all as String values
    * @param contentAuthorization Describes the authorization for a track
    * @return Eventual result containing a Set of MediaUrl objects.
    */
  def trackStreamUrlsFor(
      session: UserSession,
      trackUrn: Urn,
      requestProperties: Params,
      contentAuthorization: ContentAuthorization
  ): Future[Set[MediaUrl]] = {
    val restrictions = contentAuthorization.getContentRestrictions.toArray.map(r => r.toString())

    val params = requestProperties ++ Params(
      "method" -> "GET",
      "legacy" -> "false",
      "content_policy" -> contentAuthorization.getPolicy.toString(),
      "content_restrictions" -> ListParam(restrictions.toList)
    )

    if (contentAuthorization.getPolicy == ContentPolicy.BLOCK) {
      Future.value(Set.empty)
    } else {
      jsonClient.getWithSession(session, Path("/media") / trackUrn / "streams", params, Headers.empty).map {
        response: Response =>
          response.status match {
            case Status.Ok => trackStreamMapper.map(Json.parse(response.contentString))
            case Status.NotFound => Set()
            case _ => throw new RuntimeException(s"media-urlgen http ${response.statusCode}")
          }
      }
    }
  }
}
