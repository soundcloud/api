package com.soundcloud.bff.nextbff.mapping

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import play.api.libs.json.{JsNull, Json}

class JsonMappingSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val context = mock[MappingContext]
    val json = Json.obj("a" -> JsNull)
    val mapping = new JsonMapping(json) {}
  }

  "doesn't render the json field containing JsNull" in new Context {
    UntypedJson.write(mapping) ==== "{}"
  }

  "doesn't render URN in pieces" in new Context {
    UntypedJson.write(Urn("soundcloud", "tracks", "123")) ==== "\"soundcloud:tracks:123\""
  }

}