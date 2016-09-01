package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.test.UnitSpecification

class EmbeddedListSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val context = mock[MappingContext]
    val owner = mock[Mapper[Any, Mapping]]
    val param1 = "a"
    val param2 = "b"
    val params = List(param1, param2)
    val embedded = EmbeddedList(owner, params)
  }

  "#params returns the parameters used to create it" in new Context {
    embedded.params ==== params
  }

  "#isValid returns if the embbeded was found during materialization" >> {
    "defined" in new Context {
      embedded.materialize(Map(param1 -> new Mapping {}, param2 -> new Mapping {}))
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
      embedded.materialize(Map(param1 -> new Mapping {}, param2 -> new Mapping {}))
      embedded.isMaterialized ==== true
    }
  }
}
