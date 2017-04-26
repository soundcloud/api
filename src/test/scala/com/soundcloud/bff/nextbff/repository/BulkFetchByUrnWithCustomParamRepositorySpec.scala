package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, Json}

class BulkFetchByUrnWithCustomParamRepositorySpec extends UnitSpecification {

  "#bulkfetch" >> {

    trait Context extends Scope {

      case class CustomFetchInput(u: Urn)

      class TestRepository extends BulkFetchByUrnWithCustomParamRepository[CustomFetchInput] {

        override def fetch(session: UserSession, params: Set[CustomFetchInput]): Future[List[JsObject]] =
          Future.value(List(json1, json2))

        override def extractUrnFromParam(param: CustomFetchInput) = param.u
      }

      val session = mock[UserSession]
      val input1 = CustomFetchInput(new Urn("soundcloud:users:1"))
      val input2 = CustomFetchInput(new Urn("soundcloud:users:2"))
      val inputs = Set(input1, input2)

      val json1 = Json.obj("self" -> Json.obj("urn" -> "soundcloud:users:1"))
      val json2 = Json.obj("self" -> Json.obj("urn" -> "soundcloud:users:2"))
      val repository = new TestRepository
    }

    "returns customInput->entityJson map" in new Context {
      val bulk = Await.result(repository.bulkFetch(session, inputs))
      bulk ==== Map(input1 -> json1, input2 -> json2)
    }
  }
}
