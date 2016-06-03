package com.soundcloud.publicApiStrangler.support

import  com.twitter.finagle.http.Method
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}

class ExceptForTrackUploadsFilter(wrappedFilter: SimpleFilter[Request, RouterResponse]) extends SimpleFilter[Request, RouterResponse] {
  val PUT_TRACKS_PATTERN = "\\A/tracks/.*".r

  override def apply(request: Request, next: Service[Request, RouterResponse]) =
    (request.method, request.path) match {
      case (Method.Post, "/tracks") => next(request)
      case (Method.Put, PUT_TRACKS_PATTERN()) => next(request)
      case _ => wrappedFilter(request, next)
    }
}
