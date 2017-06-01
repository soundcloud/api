package com.soundcloud.publicApiStrangler.support

import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * Created by benjamin on 5/30/17.
  */
object UntypedJson {
  val mapper = new JsonMapper().getMapper.registerModule(DefaultScalaModule)

  /**
    * Writes an object as json string. Expected to be dual to `fromString`.
    */
  @deprecated("Use stringify with a JsValue", "43.0.0")
  def write(m: Any): String = mapper.writeValueAsString(m)

  /**
    * Alias to `write`.
    */
  @deprecated("Use stringify with a JsValue", "43.0.0")
  def asString(m: Any): String = write(m)
}
