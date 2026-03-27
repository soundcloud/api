package com.soundcloud.apipublic.client.cloudrun

import com.google.auth.oauth2.{IdTokenCredentials, ServiceAccountCredentials}
import com.twitter.util.{Future, FuturePools}

import java.io.ByteArrayInputStream
import java.util.Base64

case class CloudRunCredentialsProvider(serviceUrl: String, getCredentials: () => String) {

  private def parseCredentials: ServiceAccountCredentials = {
    val encoded = getCredentials()
    val key = new String(Base64.getDecoder.decode(encoded))
    val stream = new ByteArrayInputStream(key.getBytes())
    ServiceAccountCredentials.fromStream(stream)
  }

  private val idTokenProvider = IdTokenCredentials
    .newBuilder()
    .setIdTokenProvider(parseCredentials)
    .setTargetAudience(serviceUrl)
    .build()

  def getAccessToken: Future[String] = {
    FuturePools
      .unboundedPool() {
        idTokenProvider.refreshIfExpired()
      }
      .map(_ => Option(idTokenProvider.getAccessToken).flatMap(t => Option(t.getTokenValue)))
      .flatMap({
        case Some(token) => Future.value(token)
        case None => Future.exception(new IllegalStateException("GCP CloudRun Access token not refreshed"))
      })
  }
}
