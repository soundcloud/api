package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.Urn; import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.{Await, Future}

class InputValidationSpec extends UnitSpecification {
  "#map" >> {
    trait Context extends Scope {
      implicit val mappingContext = mock[MappingContext]
    }

    "given inputs param is empty" >> {
      trait TestContext extends Context {
        val mapper = new Mapper[Urn, Mapping] with InputValidation[Urn, Mapping] {
          override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(
              implicit context: MappingContext
          ): Future[Map[Urn, Mapping]] =
            ???
        }
      }

      "returns empty map" in new TestContext {
        val actual = Await.result(mapper.map(anonymousSession, Set.empty))
        actual ==== Map.empty
      }
    }

    "given inputs param is NOT empty" >> {
      trait TestContext extends Context {
        val urn = Urn("soundcloud", "tracks", "1")
        val expectedInputs = Set(urn)
        val expectedResult = Future.value(Map(urn -> new Mapping {}))

        val mapper = new Mapper[Urn, Mapping] with InputValidation[Urn, Mapping] {
          override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(
              implicit context: MappingContext
          ): Future[Map[Urn, Mapping]] = {
            inputs ==== expectedInputs
            expectedResult
          }
        }
      }

      "falls back to #mapNonEmptyInputs" in new TestContext {
        mapper.map(anonymousSession, expectedInputs) ==== expectedResult
      }
    }
  }
}
