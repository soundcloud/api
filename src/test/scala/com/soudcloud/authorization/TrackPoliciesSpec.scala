package com.soudcloud.authorization

import com.soundcloud.bff.authorization.Policies
import com.soundcloud.bff.authorization.Policies.allowed
import com.soundcloud.bff.authorization.Policies.blocked
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.UserSession

import play.api.libs.json.JsObject

class TrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    def policiesFor(playback: String, metadata: String) =
      new TrackPolicies(Policies(playback, metadata))
  }

  "adds the policies field if the track is authorized" in new Context {
    val json =
      policiesFor(playback = allowed, metadata = allowed)
        .apply(session, singleTrack.as[JsObject]).get
    (json \ "user_id").as[Int] mustEqual 22346297
    (json \ "policies" \ "playback").as[String] mustEqual allowed
    (json \ "policies" \ "metadata").as[String] mustEqual allowed
  }

  "returns empty if the track isn't authorized" >> {

    "playback unauthorized" in new Context {
      val json =
        policiesFor(playback = blocked, metadata = allowed)
          .apply(session, singleTrack.as[JsObject])
      json must beEmpty
    }

    "metadata unauthorized" in new Context {
      val json =
        policiesFor(playback = allowed, metadata = blocked)
          .apply(session, singleTrack.as[JsObject])
      json must beEmpty
    }

    "playback and metadata unauthorized" in new Context {
      val json =
        policiesFor(playback = blocked, metadata = blocked)
          .apply(session, singleTrack.as[JsObject])
      json must beEmpty
    }
  }
}
