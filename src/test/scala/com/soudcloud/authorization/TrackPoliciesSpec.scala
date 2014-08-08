package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import com.soundcloud.jvmkit.policies.ContentPolicies
import com.soundcloud.jvmkit.policies.Reasons

class TrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
  }

  "adds the policies field if the track is authorized" in new Context {
    val json =
      new TrackPolicies(ContentPolicies.ALLOW)
        .apply(session, singleTrack.as[JsObject]).get
    (json \ "user_id").as[Int] mustEqual 22346297
    (json \ "policy").as[String] mustEqual "ALLOW"
  }

  "returns empty if the track isn't authorized" >> {

    "playback unauthorized" in new Context {
      val json =
        new TrackPolicies(ContentPolicies.BLOCK)
          .apply(session, singleTrack.as[JsObject])
      json must beEmpty
    }
  }
}
