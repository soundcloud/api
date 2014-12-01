package com.soundcloud.publicApiStrangler.support

import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.MediaType
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status
import com.twitter.finagle.http.Version
import com.twitter.util.Future

class RejectXmlRequestFilter extends SimpleFilter[Request, Response] {

  override def apply(request: Request, next: Service[Request, Response]) =
    if (isXmlRequest(request))
      Future.value(Response(Version.Http11, Status.NotAcceptable))
    else
      next(request)

  private def isXmlRequest(request: Request) =
    request.path.endsWith(".xml") || request.acceptMediaTypes.contains(MediaType.Xml)
}
