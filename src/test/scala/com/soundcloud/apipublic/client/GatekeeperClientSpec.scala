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
    val featureName = "featureName"
  }

  trait LoggedInUserContext extends Context {
    session.getUser returns Urn("soundcloud", "users", userId.toString)
    session.isAnonymous returns false
  }

  trait AnonymousUserContext extends Context {
    session.getUser returns null
    session.isAnonymous returns true
  }

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
    "returns true for 200 when accessible for current logged in user" in new LoggedInUserContext {
      service.head(session, Path() / "users" / userId / "features" / featureName, Params.empty, Headers.empty, None) returns
        Future(JsonResponseBuilder().status(Status.Ok).build)
      Await.result(client.isFeatureAccessible(session, featureName)) ==== true
    }

    "returns false for 404 when not accessible for current logged in user" in new LoggedInUserContext {
      service.head(session, Path() / "users" / userId / "features" / featureName, Params.empty, Headers.empty, None) returns
        Future(JsonResponseBuilder().status(Status.NotFound).build)
      Await.result(client.isFeatureAccessible(session, featureName)) ==== false
    }

    "returns true for 200 when accessible for anonymous session" in new AnonymousUserContext {
      service.head(
        session,
        Path() / "users" / "anonymous" / "features" / featureName,
        Params.empty,
        Headers.empty,
        None
      ) returns
        Future(JsonResponseBuilder().status(Status.Ok).build)
      Await.result(client.isFeatureAccessible(session, featureName)) ==== true
    }

    "returns false for 404 when not accessible for anonymous session" in new AnonymousUserContext {
      service.head(
        session,
        Path() / "users" / "anonymous" / "features" / featureName,
        Params.empty,
        Headers.empty,
        None
      ) returns
        Future(JsonResponseBuilder().status(Status.NotFound).build)
      Await.result(client.isFeatureAccessible(session, featureName)) ==== false
    }
  }
}
