package com.soudcloud.publicApiStrangler.mapper

import com.soudcloud.bff.test.fixtures.Fixtures
import com.soudcloud.publicApiStrangler.mapping.{User, Track}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject

class EntityMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val okidokiClient = mock[OkidokiClient]
    val entityMapper = mock[EntityMapper]
    val mapper = new EntityMapper(okidokiClient)
    val session = mock[UserSession]
    val urns = List(
      "soundcloud:users:123",
      "soundcloud:tracks:131352352",
      "soundcloud:playlists:123").map(Urn(_))

    val userUrns = List(
      "soundcloud:users:4037",
      "soundcloud:tracks:6457573").map(Urn(_))

    override def before = {
      when(okidokiClient.fetch(===(session), any[List[Urn]])).thenReturn(
        Future(okidokiFetch.as[List[JsObject]])
      )
    }

    def result = Await.result(mapper.materialize(session, urns))
  }

  "builds the proper mappings" in new Context {
    result.size mustEqual 3
    result.head must beAnInstanceOf[User]
  }
}
