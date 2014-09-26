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

  type TrackType = XmlTrack

  def apply(visit: VisitTrack): Option[Node] =
    apply(wrapped, visit)

  private def apply(node: Node, visit: VisitTrack): Option[Node] = {
    node match {
      case node: Elem if (isArray(node)) =>
        Some(node.copy(child = visitChild(node, visit)))
      case node: Node if (isTrack(node)) =>
        visitTrack(node, visit)
      case node: Elem =>
        visitObject(node, visit)
      case other =>
        Some(other)
    }
  }

  private def visitTrack(node: Node, visit: VisitTrack) = {
    val id = (node \ "id").text.toInt
    val urn = Urn(s"soundcloud:tracks:$id")
    visit(urn, XmlTrack(node))
  }

  private def visitChild(node: Elem, visit: VisitTrack) =
    node.child.map {
      case text: Text =>
        Some(text)
      case other =>
        apply(other, visit)
    }.flatten

  private def visitObject(node: Elem, visit: VisitTrack) =
    visitChild(node, visit) match {
      case child if (child.size != node.child.size) =>
        None
      case child =>
        Some(node.copy(child = child))
    }


  private def isTrack(node: Node) =
    (node \ "kind").text == "track"

  private def isArray(node: Elem) = {
    val typeAttVal = node.attribute("type").getOrElse(None)
    if (typeAttVal == None)
      false
    else
      typeAttVal.toString == "array"
  }


}
