package com.soundcloud.bff.nextbff.test

import com.fasterxml.jackson.annotation.JsonValue
import com.soundcloud.bff.nextbff.mapping.JsonMapping
import org.mockito.Mockito
import play.api.libs.json.Json

abstract class JsonMappingMock extends JsonMapping(null)(null) {
  @JsonValue def value: Map[String, String] = ???
}

object JsonMappingMock {
  def prepare[T <: JsonMappingMock: Manifest] = {
    val mock = Mockito.mock(manifest[T].runtimeClass).asInstanceOf[T]
    Mockito.when(mock.value).thenReturn(Map("test" -> "mock"))
    Mockito.when(mock.json).thenReturn(Json.obj("test" -> "mock"))
    mock
  }
}
