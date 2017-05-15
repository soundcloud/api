package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, LoggedInUserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.test.util.{GlobalJsonFiles, JsonFiles}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach
import play.api.libs.json.JsValue

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


  lazy val fixtureFiles: JsonFiles = GlobalJsonFiles

  def withContentsOf(prefix: String, name: String): JsValue = fixtureFiles.load(s"$prefix/$name") match {
    case Some(contents) => contents
    case None => throw new IllegalStateException(s"File [$prefix/$name] not found")
  }

}
