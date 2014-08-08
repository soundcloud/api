package com.soudcloud.authorization

import com.soudcloud.data.{JsonValue, XmlValue}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.ContentPolicies
import com.soundcloud.jvmkit.policies.ContentPolicies.{ALLOW, BLOCK}
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject

import scala.xml.Node

class TrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    def policy(value: ContentPolicies) =
      new TrackPolicies(value)
  }

  "with some xml" >> {
    trait XmlContext extends Context {
      val singleTrack = new XmlValue(singleTrackXml)
    }

    "adds the policies field if the track is authorized" in new XmlContext {
      val xml = policy(ALLOW).
        apply(session, singleTrack).get.
        raw.asInstanceOf[Node]

      (xml \ "policy").text mustEqual "ALLOW"
      (xml \ "user-id").text mustEqual "22346297"
    }

    "returns empty if the track isn't authorized" >> {

      "playback unauthorized" in new XmlContext {
        val xml = policy(BLOCK)
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
      val json = policy(ALLOW).
        apply(session, singleTrack).get.
        raw.asInstanceOf[JsObject]

      (json \ "user_id").toString() mustEqual "22346297"
      (json \ "policy").as[String] mustEqual "ALLOW"
    }

    "returns empty if the track isn't authorized" >> {

      "playback unauthorized" in new JsonContext {
        val json =
          policy(BLOCK)
            .apply(session, singleTrack)
        json must beEmpty
      }
    }
  }
}
