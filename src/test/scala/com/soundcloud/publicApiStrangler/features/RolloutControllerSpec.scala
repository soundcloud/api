package com.soundcloud.publicApiStrangler.features

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import org.jboss.netty.handler.codec.http.HttpResponseStatus

class RolloutControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val rolloutMock = mock[Rollout]
    val controller = new RolloutController(rolloutMock)
    var activationsMap: Map[String, Int] = Map("feature 1" -> 0, "feature 2" -> 33, "feature 3" -> 66)

    rolloutMock.allFeatures returns activationsMap
  }

  "activates a rollout feature successfully" in new Context {
    val response = post(controller, "/-/features", Map("name" -> "feature 1", "percentage" -> "33"))
    there was one(rolloutMock).activate("feature 1", 33)

    response.status mustEqual HttpResponseStatus.OK
  }

  "returns bad request if the required attributes are not provided" in new Context {
    post(controller, "/-/features", Map("name" -> "feature 1")).status mustEqual HttpResponseStatus.BAD_REQUEST
    post(controller, "/-/features", Map("percentage" -> "0")).status mustEqual HttpResponseStatus.BAD_REQUEST
  }

  "lists all the existing features" in new Context {
    val response = get(controller, "/-/features")

    (response.jsonBody \ "features" \\ "name").map(_.as[String]) must containAllOf(Seq("feature 1", "feature 2", "feature 3"))
    (response.jsonBody \ "features" \\ "percentage").map(_.as[Int]) must containAllOf(Seq(0, 33, 66))

    response.status mustEqual HttpResponseStatus.OK
  }

  "removes a feature" in new Context {
    val response = delete(controller, "/-/features", Map("name" -> "feature 1"))
    response.status mustEqual HttpResponseStatus.OK
    there was one(rolloutMock).delete("feature 1")
  }
}

