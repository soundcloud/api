package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.Mapping
import com.soundcloud.publicApiStrangler.test.UnitSpecification

class EmbeddedSpec extends UnitSpecification {
  class TestEmbedded extends Embedded[Mapping, String] {
    val owner = mock[Mapper[Any, Mapping]]

    def params = ???

    def isValid = ???

    def isMaterialized = ???

    def materialize(values: Map[Any, Mapping]): Unit = ???

    var value: Option[String] = None
  }

  "#get" >> {
    "returns the value if materialized" in {
      val embedded = new TestEmbedded
      embedded.value = Some("a")
      embedded.get ==== "a"
    }
    "throws exception if non-materialized" in {
      val embedded = new TestEmbedded
      embedded.get must throwA[IllegalStateException]
    }
  }
}
