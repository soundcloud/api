package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.test.fixtures.Fixtures

import scala.collection.mutable.ListBuffer
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.xml.Node
import scala.xml.Elem

class TracksXmlVisitorSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val visited = ListBuffer[(Urn, Node)]()

    def visit(urn: Urn, track: XmlTrack) = {
      visited += urn -> track.content
      Some(track.content)
    }
    def urnsAndTracks(tracks: List[Node]) =
      for (track <- tracks) yield {
        val id = (track \ "id").text.toInt
        val urn = Urn(s"soundcloud:tracks:$id")
        urn -> track
      }
  }

  "visits all track objects" >> {

    "single track xml" in new Context {
      new TracksXmlVisitor(singleTrackXml).apply(visit) mustEqual Some(singleTrackXml)
      visited.toList mustEqual urnsAndTracks(List(singleTrackXml))
    }

    "playlist xml" in new Context {
      new TracksXmlVisitor(playlistXml).apply(visit) mustEqual Some(playlistXml)
      val tracks = (playlistXml \ "tracks").theSeq.head.child.toList.collect { case node: Elem => node }
      visited.toList mustEqual urnsAndTracks(tracks)
    }

    "tracks array xml" in new Context {
      new TracksXmlVisitor(tracksArrayXml).apply(visit) mustEqual Some(tracksArrayXml)
      val tracks = tracksArrayXml.child.toList.collect { case node: Elem => node }
      visited.toList mustEqual urnsAndTracks(tracks)
    }
  }

  trait NoTracksContext extends Scope {
    def visit(urn: Urn, track: XmlTrack) = ???
  }

  "doesn't invoke the visit method if the xml hasn't a track" in new NoTracksContext {
    new TracksXmlVisitor(userXml).apply(visit) mustEqual Some(userXml)
  }
}
