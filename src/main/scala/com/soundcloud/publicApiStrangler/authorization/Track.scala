package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.policies.{ContentPolicy}
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import com.soundcloud.scalakit.json.Json
import scala.xml.Node
import scala.xml.Elem

sealed trait Track {

  type Content

  def content: Content
  def withPolicies(policies: ContentPolicy): Content
}

case class JsonTrack(content: JsValue) extends Track {

  type Content = JsValue

  def withPolicies(policies: ContentPolicy) =
    JsObject(content.as[JsObject].fields :+ policyField(policies))

  private def policyField(policies: ContentPolicy) =
    "policy" -> Json.toJsValue(policies)
}

case class XmlTrack(content: Node) extends Track {

  type Content = Node

  def withPolicies(policies: ContentPolicy) = {
    val policy = <policy>{policies.getPrintName}</policy>
    val children = content.child.toList :+ policy
    new Elem(content.prefix, content.label, content.attributes, content.scope, true, children: _*)
  }
}
