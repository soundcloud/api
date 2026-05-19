package com.soundcloud.apipublic.mapper.similarcreators

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures.similarCreatorsNonEmpty
import play.api.libs.json.Json

class SimilarCreatorsMapperSpec extends UnitSpecification {

  "maps similar creators response to user urns" in {
    val expected = List(
      Urn("soundcloud", "users", "10"),
      Urn("soundcloud", "users", "20")
    )
    SimilarCreatorsMapper(similarCreatorsNonEmpty) ==== SimilarCreators(expected)
  }

  "missing or invalid users field yields empty list" in {
    SimilarCreatorsMapper(Json.obj()) ==== SimilarCreators(Nil)
    SimilarCreatorsMapper(Json.obj("users" -> "not-array")) ==== SimilarCreators(Nil)
  }
}
