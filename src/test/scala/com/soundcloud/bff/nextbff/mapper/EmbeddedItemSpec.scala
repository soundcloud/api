package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.publicApiStrangler.test.UnitSpecification

class EmbeddedItemSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val context = mock[MappingContext]
    val owner = mock[Mapper[Any, Mapping]]
    val param = "a"
    val embedded = EmbeddedItem(owner, param)
  }

  "#params returns the parameters used to create it" in new Context {
    embedded.params ==== List(param)
  }

  "#isValid returns if the embbeded was found during materialization" >> {
    "defined" in new Context {
      val mapping = new Mapping {}
      embedded.materialize(Map(param -> mapping))
      embedded.isValid ==== true
    }
    "non-defined" in new Context {
      embedded.materialize(Map())
      embedded.isValid ==== false
    }
    "non-materialized" in new Context {
      embedded.isValid must throwA[IllegalStateException]
    }
  }

  "#isMaterialized returns true after materialization" >> {

    "value not found" in new Context {
      embedded.materialize(Map())
      embedded.isMaterialized ==== true
    }

    "value found" in new Context {
      val mapping = new Mapping {}
      embedded.materialize(Map(param -> mapping))
      embedded.isMaterialized ==== true
    }
  }
}
