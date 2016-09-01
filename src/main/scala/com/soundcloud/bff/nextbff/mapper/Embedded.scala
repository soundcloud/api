package com.soundcloud.bff.nextbff.mapper

import com.fasterxml.jackson.annotation.JsonValue
import com.soundcloud.bff.nextbff.mapping.Mapping

trait Embedded[O <: Mapping, R] extends Validatable {

  val owner: Mapper[Any, O]

  def params: List[Any]

  private[bff] def isMaterialized: Boolean
  private[bff] def materialize(values: Map[Any, O]): Unit
  private[bff] def value: Option[R]

  @JsonValue protected[bff] def get =
    value.getOrElse(nonMaterializedException)

  protected def nonMaterializedException =
    throw new IllegalStateException("Trying to render a non-materialized embedded. Please make sure you are not creating embedded objects lazily.")
}
