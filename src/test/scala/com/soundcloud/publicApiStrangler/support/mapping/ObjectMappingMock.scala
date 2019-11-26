package com.soundcloud.publicApiStrangler.support.mapping

import com.fasterxml.jackson.annotation.JsonValue
import org.mockito.Mockito
import play.api.libs.json.{JsObject, JsString, JsValue}

abstract class ObjectMappingMock[R: Manifest]
    extends ObjectMapping[R](Mockito.mock(manifest[R].runtimeClass).asInstanceOf[R])(null) {
  @JsonValue def value: Map[String, String] = ???

  def json: JsValue = ???
}

object ObjectMappingMock {
  def prepare[T <: ObjectMappingMock[_]: Manifest] = {
    val mock = Mockito.mock(manifest[T].runtimeClass).asInstanceOf[T]
    Mockito.when(mock.value).thenReturn(Map("test" -> "mock"))
    Mockito.when(mock.json).thenReturn(JsObject(Seq("test" -> JsString("mock"))))
    mock
  }
}
