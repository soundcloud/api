package com.soundcloud.bff.nextbff.mapping

import com.fasterxml.jackson.annotation.JsonIgnore
import com.soundcloud.bff.nextbff.mapper.{EmbeddedList, Validatable}
import language.implicitConversions

abstract class Mapping(implicit val context: MappingContext) extends Validatable {
  /**
    * Allows the mapper to know after materialization whether or not the finished object is valid
    */
  @JsonIgnore
  override def isValid = true

  implicit protected def toMappingList[T <: Mapping](list: EmbeddedList[T]) = MappingList[T](list)
}
