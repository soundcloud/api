package com.soudcloud.authorization

import com.soudcloud.data.ParsedValue
import com.soundcloud.scalakit.Urn

trait TracksVisitor {

  def apply(data: ParsedValue): Option[ParsedValue] =
    data match {
      case (a: ParsedValue) if a.isArray =>
        visitArray(a)
      case (potentialTrack: ParsedValue) if isTrack(potentialTrack) =>
        visitTrack(potentialTrack)
      case (obj: ParsedValue) if obj.isObject =>
        visitObject(obj)
      case other =>
        Some(other)
    }

  protected def visit(urn: Urn, track: ParsedValue): Option[ParsedValue]

  private def visitTrack(data: ParsedValue) = {
    val id = data.value("id").get
    val urn = Urn(s"soundcloud:tracks:$id")
    visit(urn, data)
  }

  private def visitArray(data: ParsedValue): Option[ParsedValue] = {
    val items = data.children.map(apply).flatten.toList
    Some(data.withChildren(items))
  }

  private def visitObject(data: ParsedValue): Option[ParsedValue] =
    visitFields(data).toList match {
      case Nil =>
        None
      case fields =>
        Some(data.withChildren(fields))
    }

  private def visitFields(data: ParsedValue): Seq[ParsedValue] =
    data.children.map(e => apply(e)).flatten

  private def isTrack(data: ParsedValue) =
    data.isObject && data.value("kind") == Some("track")
}
