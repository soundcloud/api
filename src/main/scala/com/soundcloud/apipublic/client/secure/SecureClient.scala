package com.soundcloud.apipublic.client.secure

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

/**
  * The client to SoundCloud's OAuth Server
  *
  * @param client
  */
class SecureClient(client: Service[Request, Response]) {

  private val mandatoryHeaders = Map(
    "Host" -> "api-public.soundcloud.com",
    "Referer" -> "soundcloud.com"
  )

  def forwardToNewTokenEndpoint(request: Request): Future[Response] = {
    val forwardedRequest = Request(request.method, "/oauth/token")
    forwardedRequest.setContentString(request.contentString)
    val headers = request.headerMap.toMap ++ mandatoryHeaders
    headers.foreach(item => forwardedRequest.headerMap.set(item._1, item._2))

    client(forwardedRequest)
  }
}
