package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff._
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit._
import com.twitter.util.Await

class IndividualFetchSpec extends UnitSpecification {

  "#map" >> {

    trait Context extends Scope {
      implicit val mappingContext = mock[MappingContext]
    }

    "there is only one input" >> {
      trait TestContext extends Context {

        val urn = Urn("soundcloud:tracks:1")
        val expectedResult = new Mapping {}

        val mapper = new Mapper[Urn, Mapping] with InputValidation[Urn, Mapping] with IndividualFetch[Urn, Mapping] {
          override def mapSingleInput(session: UserSession, input: Urn)(implicit context: MappingContext): Future[Mapping] = {
            input ==== urn
            Future.value(expectedResult)
          }
        }
      }

      "returns the resource" in new TestContext {
        Await.result(mapper.map(anonymousSession, Set(urn))) ==== Map(urn -> expectedResult)
      }
    }

    "there are multiple inputs" >> {
      trait TestContext extends Context {

        val urn1 = Urn("soundcloud:tracks:1")
        val urn2 = Urn("soundcloud:tracks:2")
        val urn3 = Urn("soundcloud:tracks:3")
        val expectedResult = new Mapping {}

        val mapper = new Mapper[Urn, Mapping] with InputValidation[Urn, Mapping] with IndividualFetch[Urn, Mapping] {
          override def mapSingleInput(session: UserSession, input: Urn)(implicit context: MappingContext): Future[Mapping] = {
            Future.value(expectedResult)
          }
        }
      }

      "returns the resource" in new TestContext {
        val actual = Await.result(mapper.map(anonymousSession, Set(urn1, urn2, urn3)))
        actual ==== Map(urn1 -> expectedResult, urn2 -> expectedResult, urn3 -> expectedResult)
      }
    }
  }
}