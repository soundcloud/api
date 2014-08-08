package com.soudcloud.data

import com.soundcloud.jvmkit.policies.ContentPolicies

import scala.xml.{Elem, Node}

case class XmlValue(override val raw: Node) extends ParsedValue {

  override def isArray = false
  override def isObject = raw.isInstanceOf[Elem]

  def stringify = raw.toString()

  override def + (policy: ContentPolicies) = this.withChildren(children ++ policiesField(policy))

  override def value(fieldName: String) = Some((raw \ fieldName).text)

  override def children =
    raw.child.map { v =>
      new XmlValue(v)
    }

  override def withChildren(fields: Seq[ParsedValue]): ParsedValue = {
    new XmlValue(buildElem(fields.asInstanceOf[Seq[XmlValue]]))
  }

  private def policiesField(policy: ContentPolicies): Seq[ParsedValue] =
    Seq(
      new XmlValue( // MUAHAHUAHUAHUA
        <policy>{policy.getPrintName}</policy>
      )
    )

  private def buildElem(children: Seq[XmlValue]) = {
    val rawChildren = children.map(_.raw)
    new Elem(raw.prefix, raw.label, raw.attributes, raw.scope, true, rawChildren:_*)
  }

}
