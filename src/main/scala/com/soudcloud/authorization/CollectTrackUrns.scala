package com.soudcloud.authorization

import com.soudcloud.data.{Parser, ParsedValue}

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.soundcloud.scalakit.Urn

object CollectTrackUrns {

  def apply(content: String): Option[(ParsedValue, List[Urn])] =
    Try(Parser(content)) match {
      case Success(parsedContent) => collectUrns(parsedContent)
      case Failure(_) => None
    }

  private def collectUrns(parsedResponse: ParsedValue) =
    extractUrns(parsedResponse) match {
      case Nil =>
        None
      case urns =>
        Some(parsedResponse, urns)
    }

  private def extractUrns(parsedResponse: ParsedValue) = {
    val urns = ListBuffer[Urn]()
    urnsVisitor(urns).apply(parsedResponse)
    urns.toList
  }

  private def urnsVisitor(urns: ListBuffer[Urn]) =
    new TracksVisitor {
      def visit(urn: Urn, track: ParsedValue) = {
        urns += urn
        Some(track)
      }
    }
}
