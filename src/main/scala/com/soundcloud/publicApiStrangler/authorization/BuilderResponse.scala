package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.twitter.finagle.http.Response

trait BuilderResponse {
  val content: String

  def render = withBody(content)

  def withBody(body: String): Response

}

object BuilderResponse {
  def apply(body: String) =
    body match {
      case CallbackResponse.pattern(name, content) =>
        CallbackResponse(name, content)
      case other =>
        NormalResponse(body)
    }
}

case class CallbackResponse(name: String, content: String) extends BuilderResponse {
  def withBody(body: String) = ResponseBuilder.ok(s"/**/$name($body);")
}

object CallbackResponse {
  val pattern = """\/\*\*\/(.*?)\((.*)\);""".r
}

case class NormalResponse(content: String) extends BuilderResponse {
  def withBody(body: String) = ResponseBuilder.ok(body)
}