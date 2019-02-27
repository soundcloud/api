package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.nextbff.repository.BulkFetchRepository
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before
import play.api.libs.json.JsValue

class FetchMapperSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    implicit val context = mock[MappingContext]
    val session = mock[UserSession]
    val repositoryMock = mock[BulkFetchRepository[Urn]]
    val urn1 = Urn("soundcloud", "users", "3333")
    val urn2 = Urn("soundcloud", "users", "3334")
    val urns = Set(urn2, urn1)
    val jsonMap = Map(urn1 -> mock[JsValue], urn2 -> mock[JsValue])

    case class TestMapping(urn: Urn, json: JsValue) extends Mapping

    val mapper = new FetchMapper[Urn, TestMapping] {

      val repository = repositoryMock

      def map(param: Urn, jsValue: JsValue)(implicit context: MappingContext) =
        TestMapping(param, jsValue)
    }

    override def before: Any = {
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
