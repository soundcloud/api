package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.JsonResponse
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.{JsArray, JsValue}

/**
  * Retrieves waveform urls for tracks. It has the logic to deal with Content Policy = SNIP in which case it will
  * replace regular waveform with preview waveform.
  *
  * Eventually the task of detecting which waveforms should be used for a track will move to
  * [[https://github.com/soundcloud/media-service media-service]]. For now this class performs the checks based on
  * the [[com.soundcloud.publicApiStrangler.authorization.policies.ContentAuthorization]].
  *
  * @param moshimoshi         Moshimoshi client. Can be used to get track uid in case not available.
  * @param mediaServiceClient Media Service client.
  */
class WaveformUrlsRepository(moshimoshi: JsonClient, mediaServiceClient: MediaServiceClient) {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  /**
    * Because MediaServiceClient is package private users will typically
    * use this constructor.
    *
    * @param mediaService Media service.
    */
  def this(moshimoshi: JsonClient, mediaService: JsonClient) {
    this(moshimoshi, new MediaServiceClient(mediaService))
  }

  /**
    * Fetch waveform urls for one or more tracks.
    *
    * @param session             User session.
    * @param contentPoliciesById Map with ContentPolicy instances indexed by track uid (trackUid -> ContentPolicy).
    *                            We will retrieve waveform urls for all track ids.
    * @return Future set of track waveform urls. Will contain one TrackWaveformUrl object for each track which contains
    *         both json, png representation urls and optional duration.
    */
  def fetchWaveformUrls(session: UserSession, contentPoliciesById: Map[String, ContentPolicy]): Future[Set[TrackWaveformUrl]] = {
    mediaServiceClient.trackWaveformUrlsFor(session, contentPoliciesById.keys.toList) map {
      waveformUrls =>
        snipIfNecessary(waveformUrls, contentPoliciesById).filter(u => u.isPreview == false)
    }
  }

  /**
    * Fetches waveform urls for one or more tracks and index them by track uid.
    *
    * @param session             User session.
    * @param contentPoliciesById Map with ContentPolicy instances indexed by track uid (trackUid -> ContentPolicy).
    *                            We will retrieve waveform urls for all track ids.
    * @return Future map of track waveform urls indexed by track uid. Each TrackWaveformUrl object contains both json
    *         and png representation urls and optional duration.
    */
  def fetchWaveformUrlsToMap(session: UserSession, contentPoliciesById: Map[String, ContentPolicy]): Future[Map[String, TrackWaveformUrl]] = {
    fetchWaveformUrls(session, contentPoliciesById).map(urls => urls.map(url => (url.trackUid, url)).toMap)
  }

  /**
    * Fetches waveform urls. Use this in case you don't have access to the track uid but only to the urn.
    * It will additionally go to moshimoshi/okidoki to fetch track uid to allow fetching waveform urls.
    *
    * @param session     User session.
    * @param contentAuth Set of content authorizations. One for each track of interest.
    * @return Future map of track waveform urls indexed by urn.
    */
  def fetchWaveformUrls(session: UserSession, contentAuth: Set[ContentAuthorization]): Future[Map[Urn, TrackWaveformUrl]] = {

    val uidByUrn = fetchByUrns(session, moshimoshi, contentAuth.map(_.getUrn))
    val contentPoliciesByUid = uidByUrn.map(map => contentAuth.map(ca => map(ca.getUrn) -> ca.getPolicy).toMap)
    val trackUrls = contentPoliciesByUid.flatMap(map => fetchWaveformUrls(session, map))
    val urnByUid = uidByUrn.map(uidByUrnMap => uidByUrnMap.map(_.swap))

    urnByUid.flatMap(urnByUidMap => trackUrls.map(urls => urls.map(url => urnByUidMap(url.trackUid) -> url).toMap))
  }

  private val defaultBatchSize = 50

  private def fetchByUrns(session: UserSession, service: JsonClient, trackUids: Set[Urn],
                          batchSize: Int = defaultBatchSize): Future[Map[Urn, String]] = {
    inBatches(trackUids, batchSize) {
      urnBatch =>
        moshimoshi.getWithSession(session, Path("/tracks") / "fetch", urnBatch, Headers.empty).map(JsonResponse.from).map {
          case JsonResponse(Status.Ok, Right(JsArray(tracksJson)), _) =>
            extractUids(tracksJson)
        }
    }
  }

  private def inBatches[T](urns: Set[Urn], batchSize: Int)(f: (Set[Urn] => Future[Map[Urn, String]])): Future[Map[Urn, String]] = {
    Future.collect {
      urns.grouped(batchSize).toList.map(f)
    }.map(_.flatten.toMap)
  }

  private def extractUids(tracksJson: Seq[JsValue]): Map[Urn, String] = {
    tracksJson.map(trackJson => {
      val trackUid = (trackJson \ "uid").as[String]
      val trackUrn = new Urn((trackJson \ "self" \ "urn").as[String])
      trackUrn -> trackUid
    }
    ).toMap
  }

  private def snipIfNecessary(waveformUrls: Set[TrackWaveformUrl], contentPoliciesById: Map[String, ContentPolicy]): Set[TrackWaveformUrl] = {
    val waveformsByUid = waveformUrls.groupBy(url => url.trackUid)
    waveformsByUid.keys.flatMap(
      trackUid => snipIfNecessaryForTrack(waveformsByUid(trackUid), contentPoliciesById(trackUid))
    ).toSet
  }

  private def snipIfNecessaryForTrack(waveForms: Set[TrackWaveformUrl], contentPolicy: ContentPolicy): Set[TrackWaveformUrl] = {
    contentPolicy match {
      case ContentPolicy.SNIP => replaceRegularUrlsWithSnipForTrack(waveForms)
      case _ => waveForms
    }
  }

  private def replaceRegularUrlsWithSnipForTrack(waveFormUrls: Set[TrackWaveformUrl]): Set[TrackWaveformUrl] = {
    val previewUrl = waveFormUrls.filter(url => url.isPreview).headOption
    previewUrl match {
      case None => Set()
      case Some(preview) => waveFormUrls.map(url => TrackWaveformUrl(url.trackUid, preview.jsonUrl, preview.pngUrl, url.label, preview.durationMs))
    }
  }

}
