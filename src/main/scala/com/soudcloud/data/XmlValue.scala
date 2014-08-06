package com.soudcloud.data

import com.soundcloud.bff.authorization.Policies

import scala.xml.{Elem, Node}

case class XmlValue(override val raw: Node) extends ParsedValue {

  override def isArray = false
  override def isObject = raw.isInstanceOf[Elem]

  def stringify = raw.toString()

  override def + (policies: Policies) = this.withChildren(children ++ policiesField(policies))

  override def value(fieldName: String) = Some((raw \ fieldName).text)

  override def children =
    raw.child.map { v =>
      new XmlValue(v)
    }

  override def withChildren(fields: Seq[ParsedValue]): ParsedValue = {
    new XmlValue(buildElem(fields.asInstanceOf[Seq[XmlValue]]))
  }

  private def policiesField(policies: Policies): Seq[ParsedValue] =
    Seq(
      new XmlValue( // MUAHAHUAHUAHUA
        <policies>
          <playback>{policies.playback}</playback>
          <metadata>{policies.metadata}</metadata>
        </policies>
      )
    )

  private def buildElem(children: Seq[XmlValue]) = {
    val rawChildren = children.map(_.raw)
    new Elem(raw.prefix, raw.label, raw.attributes, raw.scope, true, rawChildren:_*)
  }

}
