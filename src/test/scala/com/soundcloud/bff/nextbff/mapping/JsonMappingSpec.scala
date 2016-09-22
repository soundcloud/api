package com.soundcloud.bff.nextbff.mapping

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.json.UntypedJson
import play.api.libs.json.{JsNull, Json => PlayJson}

class JsonMappingSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val context = mock[MappingContext]
    val json = PlayJson.obj("a" -> JsNull)
    val mapping = new JsonMapping(json) {}
  }

  "doesn't render the json field" in new Context {
    UntypedJson.write(mapping) ==== "{}"
  }
}
