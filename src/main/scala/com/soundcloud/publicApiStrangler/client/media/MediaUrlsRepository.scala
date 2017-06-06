package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.ContentAuthorization
import com.twitter.util.Future

/**
  * Retrieves stream [[MediaUrl]]s for tracks.
  *
  * Eventually the task of detecting which media files are available to a track will move to [[https://github.com/soundcloud/media-service media-service]].
  * For now this class performs the checks based on the [[ContentAuthorization]].
  *
  * @param mediaServiceClient Media Service client.
  */
class MediaUrlsRepository(mediaServiceClient: MediaServiceClient) {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  /**
    * This constructor is added for backwards compatibility.
    *
    * @param mediaService Media service.
    */
  def this(mediaService: JsonClient) {
    this(new MediaServiceClient(mediaService))
  }

  /**
    * Get all available stream urls for a given track.
    *
    * The [[MediaUrl]]s available for a given request depend on the [[ContentAuthorization]] and [[UserSession]] for the
    * current request. [[com.soundcloud.publicApiStrangler.authorization.policies.ContentRestriction]] and [[com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy]] may restrict what is available.
    *
    * @param session              User session.
    * @param trackUrn             Track urn.
    * @param contentAuthorization Content authorization for track.
    * @param https                If the returned links should be in HTTP or HTTPS, defaults to true
    * @return Future set of MediaUrls.
    */
  def byUrn(session: UserSession, trackUrn: Urn, contentAuthorization: ContentAuthorization, https: Boolean = true): Future[Set[MediaUrl]] = {
    fetchMediaUrls(session, trackUrn, Params("ssl" -> https.toString), contentAuthorization)
  }

  /**
    * Get all available stream urls for a given track.
    * Use this if you already have access to the track properties needed to fetch stream urls.
    *
    * @param session              User session.
    * @param trackUrn             Track urn.
    * @param params               User supplied request properties. https://github.com/soundcloud/media-service/tree/master/urlgen#required-query-string-parameters
    * @param contentAuthorization Content authorization for track.
    * @return Future set of Media Urls.
    */
  def fetchMediaUrls(session: UserSession, trackUrn: Urn, params: Params, contentAuthorization: ContentAuthorization): Future[Set[MediaUrl]] =
    mediaServiceClient.trackStreamUrlsFor(session, trackUrn, params, contentAuthorization)
}
