package com.soudcloud.authorization

import scala.collection.mutable.ListBuffer

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Urn

import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

class TracksVisitorSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val visited = ListBuffer[(Urn, JsObject)]()
    val visitor =
      new TracksVisitor {
        override def visit(urn: Urn, track: JsObject) = {
          visited += urn -> track
          Some(track)
        }
      }
    def urnsAndTracks(tracks: List[JsValue]) =
      for (track <- tracks) yield {
        val id = (track \ "id").as[Int]
        val urn = Urn(s"soundcloud:tracks:$id")
        urn -> track
      }
  }

  "visits all track objects" >> {

    "single track json" in new Context {
      visitor.apply(singleTrack) mustEqual Some(singleTrack)
      visited.toList mustEqual urnsAndTracks(List(singleTrack))
    }

    "playlist json" in new Context {
      visitor.apply(playlist) mustEqual Some(playlist)
      val tracks = (playlist \ "tracks").as[List[JsObject]]
      visited.toList mustEqual urnsAndTracks(tracks)
    }

    "tracks array json" in new Context {
      visitor.apply(tracksArray) mustEqual Some(tracksArray)
      visited.toList mustEqual urnsAndTracks(tracksArray.as[List[JsObject]])
    }
  }

  trait NoTracksContext extends Scope {
    val visitor =
      new TracksVisitor {
        override def visit(urn: Urn, track: JsObject) =
          throw new IllegalStateException("")
      }
  }

  "doesn't invoke the visit method if the json hasn't a track" in new NoTracksContext {
    visitor.apply(user) mustEqual Some(user)
  }
}
