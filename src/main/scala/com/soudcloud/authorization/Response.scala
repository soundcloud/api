package com.soudcloud.authorization

import play.api.libs.json.JsValue
import scala.xml.Node
import com.soundcloud.bff.finagle.ResponseBuilder

trait Response {
  val content: String
  def render = withBody(content)
  def withBody(body: String): ResponseBuilder
  protected def builder = new ResponseBuilder
}

object Response {
  def apply(body: String) =
    body match {
      case CallbackResponse.pattern(name, content) =>
        CallbackResponse(name, content)
      case other =>
        NormalResponse(body)
    }
}

case class CallbackResponse(name: String, content: String) extends Response {
  def withBody(body: String) = builder.body(s"/**/$name($body);")
}

object CallbackResponse {
  val pattern = """\/\*\*\/(.*)\((.*)\);""".r
}

case class NormalResponse(content: String) extends Response {
  def withBody(body: String) = builder.body(body)
}