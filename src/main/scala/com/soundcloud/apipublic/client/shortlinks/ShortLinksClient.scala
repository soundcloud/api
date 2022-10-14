package com.soundcloud.apipublic.client.shortlinks

import com.soundcloud.jvmkit.module.http.client.DynamicHttpClient
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class ShortLinksClient(client: DynamicHttpClient) {

  def resolveUrl(url: String): Future[Option[String]] =
    client.get(url, maxRedirects = None).map {
      case response if response.status == Status.Found => response.location
      case _ => None
    }
}
