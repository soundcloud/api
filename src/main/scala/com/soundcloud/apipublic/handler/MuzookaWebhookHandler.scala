package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.twitter.finagle.http.Response
import com.twitter.util.Future

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
class MuzookaWebhookHandler(val muzookaApiKey: String) extends Handler {

  lazy val logger = SoundCloudLoggerFactory.getLogger(this.getClass)
  def apply(request: HandlerRequest): Future[Response] = {
    // remove log once this is in production to avoid too many logs
    logger.debug(s"Muzooka: Request content is\n ${request.contentString}")
    val payloadSHA = SignatureCalculator.calculateSignature(muzookaApiKey, request.getContentString())
    val signatureSHA = request.headerMap.get("X-Signature")
    if (signatureSHA.contains(payloadSHA)) {
      Future.value(JsonResponseBuilder.ok("OK"))
    } else {
      logger.info(s"Muzooka: The payload SHA is $payloadSHA, The signature SHA is $signatureSHA")
      Future.value(JsonResponseBuilder.badRequest("Muzooka Signature Error"))
    }
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
    s"sha1=$signatureHex"
  }
}
