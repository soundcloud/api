package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.nextbff.repository.BulkFetchRepository
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.{Future, JsValue}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Await

class FetchMapperSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    implicit val context = mock[MappingContext]
    val session = mock[UserSession]
    val repositoryMock = mock[BulkFetchRepository[Urn]]
    val urn1 = new Urn("soundcloud:users:3333")
    val urn2 = new Urn("soundcloud:users:3334")
    val urns = Set(urn2, urn1)
    val jsonMap = Map(urn1 -> mock[JsValue], urn2 -> mock[JsValue])

    case class TestMapping(urn: Urn, json: JsValue) extends Mapping

    val mapper = new FetchMapper[Urn, TestMapping] {

      val repository = repositoryMock

      def map(param: Urn, jsValue: JsValue)(implicit context: MappingContext) =
        TestMapping(param, jsValue)
    }

    override def before = {
      when(repositoryMock.bulkFetch(session, Set(urn1, urn2)))
        .thenReturn(Future(jsonMap))
    }
  }

  "maps using the repository" in new Context {
    val mappings = Await.result(mapper.map(session, urns))
    for ((urn, mapping) <- mappings) {
      mapping.urn ==== urn
      jsonMap(mapping.urn) ==== mapping.json
    }
  }

}
