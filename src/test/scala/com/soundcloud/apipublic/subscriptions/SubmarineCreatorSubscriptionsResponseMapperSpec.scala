package com.soundcloud.apipublic.subscriptions

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures.submarineCreatorSubscription
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

class SubmarineCreatorSubscriptionsResponseMapperSpec extends UnitSpecification {
  "when response is 200" >> {
    trait SuccessContext extends Scope {
      val successResponse = Json.stringify(submarineCreatorSubscription)
      val response = Response(Status.Ok)
      response.setContentString(successResponse)
    }

    "maps creator subscriptions" in new SuccessContext {
      val expectedResult =
        Map(
          Urn("soundcloud", "users", "123") -> Some(
            SubmarineCreatorSubscription(false, Package("Yearly Pro plan", "pro"))
          ),
          Urn("soundcloud", "users", "2") -> None
        )
      SubmarineCreatorSubscriptionsResponseMapper(response) ==== expectedResult
    }
  }

  "when response is not successful" >> {
    trait FailureContext extends Scope {
      val response = Response(Status.NotFound)
    }

    "maps creator subscriptions" in new FailureContext {
      SubmarineCreatorSubscriptionsResponseMapper(response) ==== Map.empty
    }
  }

}
