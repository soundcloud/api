package com.soudcloud.authorization

import com.soundcloud.jvmkit.policies.ContentPolicies
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import com.soundcloud.scalakit.json.Json
import scala.xml.Node
import scala.xml.Elem

trait Track {

  type T

  def wrapped: T
  def withPolicies(policies: ContentPolicies): T

  def stringify: String = ???
}

case class JsonTrack(wrapped: JsValue) extends Track {

  type T = JsValue

  def withPolicies(policies: ContentPolicies) =
    JsObject(wrapped.as[JsObject].fields :+ policyField(policies))

  private def policyField(policies: ContentPolicies) =
    "policy" -> Json.toJsValue(policies)
}

case class XmlTrack(wrapped: Node) extends Track {

  type T = Node

  def withPolicies(policies: ContentPolicies) = {
    val policy =
      <policy>
        { policies.getPrintName }
      </policy>

    val children = wrapped.child.toList :+ policy
    new Elem(wrapped.prefix, wrapped.label, wrapped.attributes, wrapped.scope, true, children: _*)
  }

}

