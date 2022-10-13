package com.soundcloud.apipublic.client.firebase

import com.soundcloud.jvmkit.module.http.client.DynamicHttpClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class DynamicClient(client: DynamicHttpClient) {

  def fetchRedirectUrl(url: String): Future[Option[String]] =
    client.get(url, maxRedirects = None).map {
      case response if response.status == Status.Found => response.location
      case _ => None
    }
}
