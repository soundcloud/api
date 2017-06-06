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
  * Client for MediaService.
  *
  * @param mediaService        Json client.
  * @param trackStreamMapper   TrackStreamMapper
  * @param trackWaveformMapper TrackWaveformMapper
  */
private[media] class MediaServiceClient(mediaService: JsonClient, trackStreamMapper: TrackStreamUrlMapper, trackWaveformMapper: TrackWaveformUrlMapper) {

  /**
    * Client for MediaService.
    *
    * @param mediaService Json client.
    */
  def this(mediaService: JsonClient) {
    this(mediaService, new TrackStreamUrlMapper, new TrackWaveformUrlMapper)
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
  def trackStreamUrlsFor(session: UserSession, trackUrn: Urn, requestProperties: Params, contentAuthorization: ContentAuthorization): Future[Set[MediaUrl]] = {
    val restrictions = contentAuthorization.getContentRestrictions.toArray.map(r => r.toString())

    val params = requestProperties ++ Params(
      "method" -> "GET",
      "legacy" -> "false",
      "content_policy" -> contentAuthorization.getPolicy.toString(),
      "content_restrictions" -> ListParam(restrictions.toList)
    )

    if (contentAuthorization.getPolicy == ContentPolicy.BLOCK) {
      Future(Set())
    } else {
      mediaService.getWithSession(session, Path("/media") / trackUrn / "streams", params, Headers.empty).map {
        response: Response =>
          response.status match {
            case Status.Ok => trackStreamMapper.map(Json.parse(response.contentString))
            case Status.NotFound => Set()
          }
      }
    }
  }

  /**
    * Gets available track waveform urls for given track uids.
    *
    * @param trackUids Track uids. If more than 50 are supplied requests will be executed in batches of 50.
    * @return Eventual result containing sequence of urls.
    */
  def trackWaveformUrlsFor(session: UserSession, trackUids: List[String]): Future[Set[TrackWaveformUrl]] =
    fetchByUids(session, mediaService, trackUids)

  private val defaultBatchSize = 50

  private def fetchByUids(session: UserSession, service: JsonClient, trackUids: List[String],
                          batchSize: Int = defaultBatchSize): Future[Set[TrackWaveformUrl]] = {
    inBatches(trackUids, batchSize) {
      uidBatch =>
        val params = Params("uid" -> ListParam(uidBatch))
        mediaService.getWithSession(session, Path("/waveforms"), params, Headers.empty).map {
          response: Response =>
            response.status match {
              case Status.Ok => trackWaveformMapper.map(Json.parse(response.contentString))
              case Status.NotFound => Set()
            }
        }
    }
  }

  private def inBatches[T](trackUids: List[String], batchSize: Int)
                          (f: (List[String] => Future[Set[TrackWaveformUrl]])): Future[Set[TrackWaveformUrl]] = {
    Future.collect {
      trackUids.grouped(batchSize).toList.map(f)
    }.map(_.flatten.toSet)
  }
}
