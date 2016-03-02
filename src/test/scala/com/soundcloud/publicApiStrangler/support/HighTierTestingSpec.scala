package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.Request
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSessionBuilder
import com.soundcloud.jvmkit.policies.MonetizationModel
import com.twitter.finagle.http.{Request => FinagleRequest}

import scala.collection.JavaConversions._

class HighTierTestingSpec extends UnitSpecification {

  "#isEnabled" >> {

    "when monetization model is not_applicable and track is whitelisted" >> {

      "returns false if session doesn't have InternalQAFeature and req doesn't have header" >> {
        val highTierTesting = new HighTierTesting()
        val session = new UserSessionBuilder().build()
        val req = new Request(FinagleRequest("trackId" -> "244217343"))

        highTierTesting.isEnabled(MonetizationModel.NOT_APPLICABLE, session, req) must beFalse
      }

      "returns true if session has InternalQAFeature" >> {
        val InternalQAFeatureName = "internal_qa"
        val highTierTesting = new HighTierTesting()

        val session = new UserSessionBuilder().setFeatures(Set(InternalQAFeatureName)).build()
        val req = new Request(FinagleRequest("trackId" -> "244217343"))

        highTierTesting.isEnabled(MonetizationModel.NOT_APPLICABLE, session, req) must beTrue
      }

      "returns true if request has the right X-Testing-Token with the right value" >> {
        val highTierTesting = new HighTierTesting()
        val session = new UserSessionBuilder().build()

        val finagleRequest = FinagleRequest("trackId" -> "244217343")
        finagleRequest.headerMap.add("X-Testing-Token", "f4271570d8abcb6f90104e638ae182fe")
        val req = new Request(finagleRequest)

        highTierTesting.isEnabled(MonetizationModel.NOT_APPLICABLE, session, req) must beTrue
      }
    }

    "returns false when monetization model is not not_applicable" >> {
      val InternalQAFeatureName = "internal_qa"
      val highTierTesting = new HighTierTesting()

      val session = new UserSessionBuilder().setFeatures(Set(InternalQAFeatureName)).build()
      val req = new Request(FinagleRequest("trackId" -> "244217343"))

      highTierTesting.isEnabled(MonetizationModel.AD_SUPPORTED, session, req) must beFalse
      highTierTesting.isEnabled(MonetizationModel.SUB_MID_TIER, session, req) must beFalse
      highTierTesting.isEnabled(MonetizationModel.SUB_HIGH_TIER, session, req) must beFalse
    }

    "returns false when track is not white listed" >> {
      val InternalQAFeatureName = "internal_qa"
      val highTierTesting = new HighTierTesting()

      val session = new UserSessionBuilder().setFeatures(Set(InternalQAFeatureName)).build()
      val req = new Request(FinagleRequest("trackId" -> "123123123"))

      highTierTesting.isEnabled(MonetizationModel.NOT_APPLICABLE, session, req) must beFalse
    }
  }
}
