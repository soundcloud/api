package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.client.TokenDispenserClient
import com.soundcloud.apipublic.client.mothership.MoshimoshiClient
import com.soundcloud.apipublic.handler.muzooka.JsonFormats._
import com.soundcloud.apipublic.handler.muzooka.{Artists, Image, ImageDownloader, MuzookaArtist}
import com.soundcloud.apipublic.handler.support.requestParser.ArtistImageUpdateRequest
import com.soundcloud.apipublic.service.artwork.HocuspocusUtils
import com.soundcloud.hocuspocus.HocuspocusService
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.Good
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.Response
import com.twitter.finagle.util.DefaultTimer.Implicit
import com.twitter.util.{Duration, Future}
import org.slf4j.Logger
import play.api.libs.json.Json

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
Muzooka is platform for artists and their teams to manage their own media assets
across multiple platforms from one central hub. Here we would like to be updated
with the artists data and update accordingly
This endpoint acts as a webhook to receive updates that we subscribe to
For more details check their documentation https://www.muzooka.com/api-docs#tag/Webhooks
**/
class MuzookaWebhookHandler(
    val muzookaApiKey: String,
    hocuspocusService: HocuspocusService,
    moshimoshiClient: MoshimoshiClient,
    userAuthentication: UserAuthentication,
    tokenDispenserClient: TokenDispenserClient,
    imageDownloader: ImageDownloader
) extends Handler {
  private val MUZOOKA_CLIENT_CREDENTIALS_URN = s"soundcloud:credentials:295722"

  lazy val logger: Logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def apply(request: HandlerRequest): Future[Response] = {
    val payloadSHA = SignatureCalculator.calculateSignature(muzookaApiKey, request.getContentString())
    val signatureSHA = request.headerMap.get("X-Signature")

    // Check for signature before doing any work for security and making sure the request is originated from Muzooka
    if (!signatureSHA.contains(payloadSHA)) {
      logger.info(s"Muzooka: The payload SHA is $payloadSHA, The signature SHA is $signatureSHA")
      return Future.value(JsonResponseBuilder.forbidden("Muzooka Signature Error"))
    }
    val muzookaArtist = Json
      .parse(request.contentString)
      .asOpt[MuzookaArtist]
      .map(Good(_))
      .get
      .value

    val muzookaId = muzookaArtist.id
    val image = findImage(muzookaArtist).orNull
    if (image == null) {
      return Future.value(JsonResponseBuilder.notFound(s"Image not found for artist with id $muzookaId"))
    }
    val userId = Artists.ARTISTS.get(muzookaId).orNull
    if (userId == null) {
      return Future.value(JsonResponseBuilder.notFound(s"Artist not found with id $muzookaId"))
    }
    val soundCloudUrn = s"soundcloud:users:$userId"
    val imageUrl = image.url

    val maybeImageBuf = imageDownloader.downloadImageToBuf(imageUrl)
    tokenDispenserClient
      .dispenseAccessToken(Urn.parse(soundCloudUrn).get, Urn.parse(MUZOOKA_CLIENT_CREDENTIALS_URN).get)
      .delayed(Duration.fromSeconds(1))
      .flatMap(accessToken => {
        request.headerMap.add("authorization", s"OAuth ${accessToken.accessToken}")
        userAuthentication.withUserSession(request) { session =>
          uploadArtworkToS3(session, Urn.parse(soundCloudUrn).get, ArtistImageUpdateRequest(maybeImageBuf))
          Future.value(JsonResponseBuilder.ok("Image updated successfully"))
        }
      })
  }

  private def uploadArtworkToS3(
      userSession: UserSession,
      userUrn: Urn,
      artistImageUpdateRequest: ArtistImageUpdateRequest
  ) =
    hocuspocusService
      .storeImage(HocuspocusUtils.toRaw(artistImageUpdateRequest.imageData))
      .map(image => moshimoshiClient.updateUserAvatar(userSession, userUrn, image))

  private def findImage(artist: MuzookaArtist): Option[Image] = {
    val images = artist.image.orNull
    if (images == null || images.isEmpty) {
      return Option.empty
    }
    val oneByOneImages = images.filter(_.ratio.contains("1x1"))

    val exactMatch = oneByOneImages.find(img => img.width == 1280 && img.height == 1280)
    exactMatch.orElse(
      oneByOneImages
        .filter(img => img.width > 1280 && img.height > 1280)
        .sortBy(img => img.width) // or sort by area img.width * img.height for more precise size control
        .headOption
    )
  }
}
/*
The artist update requests are signed in the header with X-Signature which is
the payload of the request signed with the API-KEY
This class is responsible for calculating the payload signature
 */
object SignatureCalculator {
  def calculateSignature(key: String, payload: String): String = {
    val secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(secretKey)
    val signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
    val signatureHex = signatureBytes.map("%02X" format _).mkString
    s"sha1=$signatureHex".toLowerCase()
  }
}
