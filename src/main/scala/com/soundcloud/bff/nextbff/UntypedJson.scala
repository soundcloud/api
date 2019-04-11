package com.soundcloud.bff.nextbff

import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * This is used in combination with the nextbff stuff and was moved here from Big JVMKit
  * because no confident replacement was found.
  */
object UntypedJson {
  val mapper = new JsonMapper().getMapper.registerModule(DefaultScalaModule)

  /**
    * Writes an object as json string. Expected to be dual to `fromString`.
    */
  def write(m: Any): String = mapper.writeValueAsString(m)
}
