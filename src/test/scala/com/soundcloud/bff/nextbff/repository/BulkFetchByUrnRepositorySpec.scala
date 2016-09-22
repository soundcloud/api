package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, JsString, Json}

class BulkFetchByUrnRepositorySpec extends UnitSpecification {

  "#bulkfetch" >> {

    trait Context extends Scope {
      val session = mock[UserSession]
      val urn1 = new Urn("soundcloud:users:1")
      val urn2 = new Urn("soundcloud:users:2")
      val urns = Set(urn1, urn2)

      val json1 = Json.obj("self" -> Json.obj("urn" -> JsString("soundcloud:users:1")))
      val json2 = Json.obj("self" -> Json.obj("urn" -> JsString("soundcloud:users:2")))

      val repository = new BulkFetchByUrnRepository {
        override def fetch(session: UserSession, params: Set[Urn]): Future[List[JsObject]] =
          Future.value(List(json1, json2))
      }
    }

    "returns urn->entityJson map" in new Context {
      val bulk = Await.result(repository.bulkFetch(session, urns))
      bulk ==== Map(urn1 -> json1, urn2 -> json2)
    }
  }
}
