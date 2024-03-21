package com.soundcloud.apipublic.filter

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}

class ExceptForTrackUploadsFilter(wrappedFilter: SimpleFilter[Request, Response])
    extends SimpleFilter[Request, Response] {
  val PUT_TRACKS_PATTERN = "\\A/tracks/.*".r

  override def apply(request: Request, next: Service[Request, Response]) =
    (request.method, request.path) match {
      case (Method.Post, "/tracks") => next(request)
      case (Method.Post, "/muzooka/webhook") => next(request)
      case (Method.Put, PUT_TRACKS_PATTERN()) => next(request)
      case _ => wrappedFilter(request, next)
    }
}
