package com.soudcloud.authorization

import com.soudcloud.data.{JsonValue, XmlValue, ParsedValue}

import scala.collection.mutable.ListBuffer

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Urn

import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

import scala.xml.{Node}

class TracksVisitorSpec extends UnitSpecification with Fixtures {

  trait Context[T] extends Scope {
    val visited = ListBuffer[(Urn, T)]()
    val visitor =
      new TracksVisitor {
        override def visit(urn: Urn, track: ParsedValue) = {
          visited += urn -> track.raw.asInstanceOf[T]
          Some(track)
        }
      }
  }

  "json data" >> {
    trait JsonContext extends Context[JsValue] {
      lazy val singleTrack = new JsonValue(singleTrackJson)
      lazy val playlist = new JsonValue(playlistJson)
      lazy val tracksArray = new JsonValue(tracksArrayJson)

      def urnsAndTracks(tracks: List[JsValue]) =
        for (track <- tracks) yield {
          val id = (track \ "id").as[Int]
          val urn = Urn(s"soundcloud:tracks:$id")
          urn -> track
        }
    }

    "visits all track objects" >> {
      "single track json" in new JsonContext {
        visitor.apply(singleTrack) mustEqual Some(singleTrack)
        visited.toList mustEqual urnsAndTracks(List(singleTrack.raw))
      }

      "playlist json" in new JsonContext {
        visitor.apply(playlist) mustEqual Some(playlist)
        val tracks = (playlist.raw \ "tracks").as[List[JsObject]]
        visited.toList mustEqual urnsAndTracks(tracks)
      }

      "tracks array json" in new JsonContext {
        visitor.apply(tracksArray) mustEqual Some(tracksArray)
        visited.toList mustEqual urnsAndTracks(tracksArray.children.map(_.raw.asInstanceOf[JsValue]).toList)
      }
    }

    trait NoTracksContext extends JsonContext {
      lazy val user = new JsonValue(userJson)
      override val visitor =
        new TracksVisitor {
          override def visit(urn: Urn, track: ParsedValue) =
            throw new IllegalStateException("")
        }
    }

    "doesn't invoke the visit method if the json hasn't a track" in new NoTracksContext {
      visitor.apply(user) mustEqual Some(user)
    }
  }

  "xml data" >> {
    trait XmlContext extends Context[Node] {
      lazy val singleTrack = new XmlValue(singleTrackXml)
      lazy val playlist = new XmlValue(playlistXml)
      lazy val tracksArray = new XmlValue(tracksArrayXml)

      def urnsAndTracks(tracks: List[Node]) =
        for (track <- tracks) yield {
          val id = (track \ "id").toList.head.text
          val urn = Urn(s"soundcloud:tracks:$id")
          urn -> track
        }
    }

    "visits all track objects" >> {
      "single track xml" in new XmlContext {
        visitor.apply(singleTrack) mustEqual Some(singleTrack)
        visited.toList mustEqual urnsAndTracks(List(singleTrack.raw))
      }

      "playlist xml" in new XmlContext {
        visitor.apply(playlist).get.raw mustEqual playlist.raw
        val tracks = (playlistXml \ "tracks" \ "track").toList
        visited.toList mustEqual urnsAndTracks(tracks)
      }

      "tracks xml" in new XmlContext {
        val visitedTracks = (tracksArrayXml \ "track").toList
        visitor.apply(tracksArray) mustEqual Some(tracksArray)
        visited.toList mustEqual urnsAndTracks(visitedTracks)
      }
    }

    trait NoTracksContext extends Scope {
      lazy val user = new XmlValue(userXml)
      val visitor =
        new TracksVisitor {
          override def visit(urn: Urn, track: ParsedValue) =
            throw new IllegalStateException("")
        }
    }

    "doesn't invoke the visit method if the xml hasn't a track" in new NoTracksContext {
      visitor.apply(user) mustEqual Some(user)
    }
  }
}
