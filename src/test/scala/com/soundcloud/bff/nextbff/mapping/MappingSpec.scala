package com.soundcloud.bff.nextbff.mapping

import com.soundcloud.bff.nextbff.mapper.{EmbeddedList, Mapper}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.Urn
import language.reflectiveCalls

class MappingSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val mappingContext = mock[MappingContext]
    val urn = Urn("soundcloud", "tracks", "232")
    val mapper = mock[Mapper[Any, Mapping]]
    val embeddedList = EmbeddedList(mapper, List(urn))
  }

  "provides implicit conversion from embedded list to mapping list" in new Context {
    val mapping = new Mapping {
      val mappingList: MappingList[Mapping] = embeddedList
    }

    mapping.mappingList.embedded ==== embeddedList
  }

  "defaults as valid" in new Context {
    val mapping = new Mapping {}

    mapping.isValid must beTrue
  }
}
