package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, LoggedInUserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.twitter.finagle.http.{HeaderMap, Status}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach
import play.api.libs.json.{JsValue, Json}

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

trait UnitSpecification extends Specification with BeforeAfterEach with Mockito {

  type Scope = org.specs2.specification.Scope

  override def after: Any = {}

  override def before: Any = {}

  override def mock[T: ClassTag]: T = smartMock[T]

  private val someApp = new Urn("soundcloud:systems:1")
  private val someScopes = Set("a", "b")

  def loggedInSession(urn: Urn) = (new UserSessionBuilder)
    .setUser(urn)
    .setAgent(someApp)
    .setGeo(new Geo("US", "Mountain View", "CA"))
    .setScopes(someScopes)
    .build.asInstanceOf[LoggedInUserSession]

  def anonymousSession = (new UserSessionBuilder)
    .setAgent(someApp)
    .setScopes(someScopes)
    .build.asInstanceOf[AnonymousUserSession]

  def jsonResponse(status: Status, json: JsValue, headers: HeaderMap = HeaderMap()) =
    JsonResponseBuilder().status(status).body(Json.stringify(json)).headers(headers.toMap).build

}
