package com.soudcloud.authorization

import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsString
import scala.xml.Node
import scala.xml.Elem
import scala.xml.Text

class TracksXmlVisitor(val wrapped: Node) extends TracksVisitor {

  type T = XmlTrack

  def apply(visit: VisitTrack): Option[Node] =
    apply(wrapped, visit)

  private def apply(node: Node, visit: VisitTrack): Option[Node] = {
    node match {
      case node: Node if (isTrack(node)) =>
        visitTrack(node, visit)
      case node: Elem =>
        val att = node.attributes
        val children = node.child.map {
          case text: Text =>
            Some(text)
          case other =>
            apply(other, visit)
        }.flatten
        Some(new Elem(node.prefix, node.label, node.attributes, node.scope, true, children: _*))
      case other =>
        Some(other)
    }
  }

  private def visitTrack(node: Node, visit: VisitTrack) = {
    val id = (node \ "id").text.toInt
    val urn = Urn(s"soundcloud:tracks:$id")
    visit(urn, XmlTrack(node))
  }

  private def isTrack(node: Node) =
    (node \ "kind").text == "track"
}
