package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsValue

class IndividualFetchRepositorySpec extends UnitSpecification {

  trait Service {
    def fetch(session: UserSession, input: Urn): Future[JsValue]
  }

  trait Context extends VerifiedMocks {

    val session = mock[UserSession]
    val urn1 = new Urn("soundcloud:users:3232")
    val urn2 = new Urn("soundcloud:users:323")
    val urns = Set(urn1, urn2)

    val json1 = mock[JsValue]
    val json2 = mock[JsValue]
    val serviceMock = mock[Service]

    val repository = new IndividualFetchRepository[Urn] {
      def fetch(session: UserSession, input: Urn): Future[Option[JsValue]] =
        serviceMock.fetch(session, input).map(Option(_))
    }

    override def before =
      when(serviceMock.fetch(any, any))
        .thenReturn(Future(json1))
        .thenReturn(Future(json2))
  }

  "use individual fetches to return the bulk" in new Context {
    val bulk = Await.result(repository.bulkFetch(session, urns))
    bulk ==== Map(urn1 -> json1, urn2 -> json2)
  }
}
