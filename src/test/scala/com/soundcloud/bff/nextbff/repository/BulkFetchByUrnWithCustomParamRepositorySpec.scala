package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.Future
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.twitter.util.Await
import play.api.libs.json.{JsObject, JsString}
import play.api.libs.json.Json

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
      val input1 = CustomFetchInput(Urn("soundcloud:users:1"))
      val input2 = CustomFetchInput(Urn("soundcloud:users:2"))
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
