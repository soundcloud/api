package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import com.soundcloud.jvmkit.policies.ContentPolicies
import com.soundcloud.jvmkit.policies.Reasons

class ApplyTrackPoliciesSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope {
    val session = mock[UserSession]
    val urns =
      tracksArray.as[List[JsObject]]
        .map(track => (track \ "id").as[Int])
        .map(id => Urn(s"soundcloud:tracks:$id"))
    def rules: List[ContentAuthorization]

    lazy val authorizedTrackIds =
      extractIds(
        ApplyTrackPolicies(session, new TracksJsonVisitor(tracksArray), rules).get
      )

    def extractIds(json: JsValue) =
      json.as[List[JsObject]].map(e => (e \ "id").as[Int])
  }

  "applies the the policies to all track objects" >> {

    trait EverythingAuthorized extends Context {
      def rules =
        for (urn <- urns) yield {
          new ContentAuthorization(urn, ContentPolicies.ALLOW, Reasons.GEO)
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
            new ContentAuthorization(urn, ContentPolicies.ALLOW, Reasons.GEO)
          else
            new ContentAuthorization(urn, ContentPolicies.BLOCK, Reasons.GEO)
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual authorized.map(_.getIdentifier.toInt)
    }
  }

  "stream tests" >> {
    trait StreamContext extends Context {

      override val urns = List(165855069, 168419205).map(id => Urn(s"soundcloud:tracks:$id"))

      override lazy val authorizedTrackIds =
        extractIds(
          ApplyTrackPolicies(session, new TracksJsonVisitor(stream), rules).get
        )

      override def extractIds(json: JsValue) =
        (json \ "collection").as[List[JsObject]].map(e => (e \ "track" \ "id").asOpt[Int].getOrElse(-999))

    }

    trait PartiallyAuthorized extends StreamContext {
      val authorized = urns.take(1)
      override def rules =
        for (urn <- urns) yield {
          if (authorized.contains(urn))
            new ContentAuthorization(urn, ContentPolicies.ALLOW, Reasons.GEO)
          else
            new ContentAuthorization(urn, ContentPolicies.BLOCK, Reasons.GEO)
        }
    }

    "some authorized tracks" in new PartiallyAuthorized {
      authorizedTrackIds mustEqual Seq(165855069)
    }
  }



}
