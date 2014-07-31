package com.soudcloud.authorization

import com.soudcloud.data.{ParsedValue, JsonValue}
import com.soundcloud.bff.authorization.AuthorizationRules
import com.soundcloud.bff.authorization.Policies._
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import com.soundcloud.bff.authorization.Policies

class ApplyTrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    val urns =
      tracksArrayJson.as[List[JsObject]]
        .map(track => (track \ "id").as[Int])
        .map(id => Urn(s"soundcloud:tracks:$id"))
    def rules: List[AuthorizationRules]

    def tracksArray = new JsonValue(tracksArrayJson)

    lazy val authorizedTrackIds =
      extractIds(
        ApplyTrackPolicies(session, tracksArray, rules).get
      )

    def extractIds(wrapper: ParsedValue) =
      wrapper.children.map(e => e.value("id").head)
  }

  "applies the the policies to all track objects" >> {

    trait EverythingAuthorized extends Context {
      def rules =
        for (urn <- urns) yield {
          new AuthorizationRules(urn, Policies(allowed, allowed))
        }
    }

    "all tracks authorized" in new EverythingAuthorized {
      authorizedTrackIds mustEqual extractIds(tracksArray)
    }

    trait PartiallyAuthorized extends Context {
      val authorized = urns.take(2)
      def rules =
        for (urn <- urns) yield {
          if (authorized.contains(urn))
            new AuthorizationRules(urn, Policies(allowed, allowed))
          else
            new AuthorizationRules(urn, Policies(blocked, blocked))
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual authorized.map(_.getIdentifier)
    }
  }
}
