package com.soudcloud.authorization

import com.soudcloud.data.{XmlValue, JsonValue}
import com.soundcloud.bff.authorization.Policies
import com.soundcloud.bff.authorization.Policies.allowed
import com.soundcloud.bff.authorization.Policies.blocked
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.UserSession

import play.api.libs.json.JsObject

import scala.xml.Node

class TrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    def policiesFor(playback: String, metadata: String) =
      new TrackPolicies(Policies(playback, metadata))
  }

  "with some xml" >> {
    trait XmlContext extends Context {
      val singleTrack = new XmlValue(singleTrackXml)
    }

    "adds the policies field if the track is authorized" in new XmlContext {
      val xml = policiesFor(playback = allowed, metadata = allowed).
        apply(session, singleTrack).get.
        raw.asInstanceOf[Node]

      (xml \ "policies" \ "playback").text mustEqual allowed
      (xml \ "policies" \ "metadata").text mustEqual allowed
      (xml \ "user-id").text mustEqual "22346297"
    }

    "returns empty if the track isn't authorized" >> {

      "playback unauthorized" in new XmlContext {
        val xml =
          policiesFor(playback = blocked, metadata = allowed)
            .apply(session, singleTrack)
        xml must beEmpty
      }

      "metadata unauthorized" in new XmlContext {
        val xml =
          policiesFor(playback = allowed, metadata = blocked)
            .apply(session, singleTrack)
        xml must beEmpty
      }

      "playback and metadata unauthorized" in new XmlContext {
        val xml =
          policiesFor(playback = blocked, metadata = blocked)
            .apply(session, singleTrack)
        xml must beEmpty
      }
    }
  }

  "with some json" >> {
    trait JsonContext extends Context {
      val singleTrack = new JsonValue(singleTrackJson)
    }

    "adds the policies field if the track is authorized" in new JsonContext {
      val json = policiesFor(playback = allowed, metadata = allowed).
        apply(session, singleTrack).get.
        raw.asInstanceOf[JsObject]

      (json \ "user_id").toString() mustEqual "22346297"
      (json \ "policies" \ "playback").as[String] mustEqual allowed
      (json \ "policies" \ "metadata").as[String] mustEqual allowed
    }

    "returns empty if the track isn't authorized" >> {

      "playback unauthorized" in new JsonContext {
        val json =
          policiesFor(playback = blocked, metadata = allowed)
            .apply(session, singleTrack)
        json must beEmpty
      }

      "metadata unauthorized" in new JsonContext {
        val json =
          policiesFor(playback = allowed, metadata = blocked)
            .apply(session, singleTrack)
        json must beEmpty
      }

      "playback and metadata unauthorized" in new JsonContext {
        val json =
          policiesFor(playback = blocked, metadata = blocked)
            .apply(session, singleTrack)
        json must beEmpty
      }
    }
  }
}
