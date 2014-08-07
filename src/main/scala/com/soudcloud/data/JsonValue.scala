package com.soudcloud.data

import com.soundcloud.jvmkit.policies.ContentPolicies
import com.soundcloud.scalakit.json.Json
import play.api.libs.json._

case class JsonValue(override val raw: JsValue) extends ParsedValue {

  def stringify = Json.stringify(raw)

  override def value(fieldName: String) =
    (raw \ fieldName) match {
      case x: JsString => Some(x.value)
      case x: JsNumber => Some(x.toString())
      case other => None
    }

  override def isArray = raw.isInstanceOf[JsArray]

  override def isObject = raw.isInstanceOf[JsObject]

  override def children: Seq[ParsedValue] =
    raw match {
      case x: JsObject => x.fields.map({ case (k,v) => new JsonValue(v) })
      case a: JsArray => a.value.map(JsonValue)
      case _ => Seq.empty
    }

  override def withChildren(fields: Seq[ParsedValue]) = {
    new JsonValue(
      buildJson(this, fields.asInstanceOf[Seq[JsonValue]])
    )
  }

  private def buildJson(parent: JsonValue, childrenToKeep: Seq[JsonValue]): JsValue = {
    val toKeep = childrenToKeep.map(_.raw)

    raw match {
      case o: JsObject =>
        val rawChildren = o.fields.filter({ case(k, v) => toKeep.contains(v) })
        JsObject(rawChildren)

      case a: JsArray =>
        JsArray(toKeep)

      case _ => JsNull
    }
  }

  override def + (policies: ContentPolicies) =
    new JsonValue(raw.as[JsObject] + policyField(policies))

  private def policyField(policy: ContentPolicies) = "policy" -> Json.toJsValue(policy)
}