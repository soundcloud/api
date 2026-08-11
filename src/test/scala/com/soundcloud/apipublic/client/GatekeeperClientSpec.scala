package com.soundcloud.apipublic.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsString}

class GatekeeperClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]
    val client = new GatekeeperClient(service)
    val userId = 123
    val userUrn = Urn("soundcloud", "users", userId.toString)
    val featureName = "featureName"
  }

  trait LoggedInUserContext extends Context {
    session.getUser returns userUrn
    session.isAnonymous returns false
  }

  trait AnonymousUserContext extends Context {
    session.getUser returns null
    session.isAnonymous returns true
  }

  def featureResponse(features: String*) =
    Future(
      JsonResponseBuilder()
        .status(Status.Ok)
        .body(JsArray(features.map(JsString.apply)).toString)
        .build
    )

  "#featuresFor" >> {
    "returns list of feature names for current logged in user" in new LoggedInUserContext {
      service.getWithSession(session, Path() / "users" / userId / "features", Params.empty, Headers.empty) returns
        Future(
          JsonResponseBuilder()
            .status(Status.Ok)
            .body(JsArray(Seq(JsString("foo"), JsString("bar"), JsString("baz"))).toString)
            .build
        )
      Await.result(client.featuresFor(session)) ==== Set("foo", "bar", "baz")
    }

    "returns list of feature names for anonymous when session is anonymous" in new AnonymousUserContext {
      service.getWithSession(session, Path() / "users" / "anonymous" / "features", Params.empty, Headers.empty) returns
        Future(
          JsonResponseBuilder()
            .status(Status.Ok)
            .body(JsArray(Seq(JsString("foo"), JsString("bar"), JsString("baz"))).toString)
            .build
        )
      Await.result(client.featuresFor(session)) ==== Set("foo", "bar", "baz")
    }
  }

  "#isFeatureAccessible" >> {
    "returns true when the feature is listed for the current logged in user" in new LoggedInUserContext {
      service.getWithSession(session, Path() / "users" / userUrn.toString / "features", Params.empty, Headers.empty) returns
        featureResponse("otherFeature", featureName)

      Await.result(client.isFeatureAccessible(session, featureName)) ==== true

      there was no(service).head(any, any, any, any, any)
    }

    "returns false when the feature is not listed for the current logged in user" in new LoggedInUserContext {
      service.getWithSession(session, Path() / "users" / userUrn.toString / "features", Params.empty, Headers.empty) returns
        featureResponse("otherFeature")

      Await.result(client.isFeatureAccessible(session, featureName)) ==== false
    }

    "returns true when the feature is listed for an anonymous session" in new AnonymousUserContext {
      service.getWithSession(session, Path() / "users" / "anonymous" / "features", Params.empty, Headers.empty) returns
        featureResponse(featureName)

      Await.result(client.isFeatureAccessible(session, featureName)) ==== true
    }

    "returns false when the feature is not listed for an anonymous session" in new AnonymousUserContext {
      service.getWithSession(session, Path() / "users" / "anonymous" / "features", Params.empty, Headers.empty) returns
        featureResponse("otherFeature")

      Await.result(client.isFeatureAccessible(session, featureName)) ==== false
    }
  }
}
