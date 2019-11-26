package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import play.api.libs.json.{JsObject, JsValue}

import scala.collection.mutable.ListBuffer

class TracksVisitorSpec extends UnitSpecification {
  trait Context extends Scope {
    val visited = ListBuffer[(Urn, JsValue)]()

    def visit(urn: Urn, track: Track) = {
      visited += urn -> track.json
      Some(track.json)
    }

    def urnsAndTracks(tracks: List[JsValue]) =
      for (track <- tracks) yield {
        val id = (track \ "id").as[Int]
        val urn = Urn("soundcloud", "tracks", id.toString)
        urn -> track
      }
  }

  "visits all track objects" >> {
    "single track json" in new Context {
      new TracksVisitor(singleTrack).apply(visit) mustEqual Some(singleTrack)
      visited.toList mustEqual urnsAndTracks(List(singleTrack))
    }

    "playlist json" in new Context {
      new TracksVisitor(playlist).apply(visit) mustEqual Some(playlist)
      val tracks = (playlist \ "tracks").as[List[JsObject]]
      visited.toList mustEqual urnsAndTracks(tracks)
    }

    "tracks array json" in new Context {
      new TracksVisitor(tracksArray).apply(visit) mustEqual Some(tracksArray)
      visited.toList mustEqual urnsAndTracks(tracksArray.as[List[JsObject]])
    }
  }

  trait NoTracksContext extends Scope {
    def visit(urn: Urn, track: Track) = ???
  }

  "doesn't invoke the visit method if the json hasn't a track" in new NoTracksContext {
    new TracksVisitor(user).apply(visit) mustEqual Some(user)
  }
}
