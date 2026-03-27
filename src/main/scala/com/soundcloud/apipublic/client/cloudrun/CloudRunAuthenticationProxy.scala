package com.soundcloud.apipublic.client.cloudrun

import com.twitter.finagle.{Service, ServiceProxy}
import com.twitter.finagle.http.{Fields, Request, Response}
import com.twitter.util.Future

case class CloudRunAuthenticationProxy(
    credentialsProvider: CloudRunCredentialsProvider,
    underlying: Service[Request, Response]
) extends ServiceProxy[Request, Response](underlying) {

  override def apply(request: Request): Future[Response] = {
    credentialsProvider.getAccessToken.flatMap(idToken => {
      request.headerMap.add(Fields.Authorization, s"Bearer $idToken")
      underlying(request)
    })
  }
}
