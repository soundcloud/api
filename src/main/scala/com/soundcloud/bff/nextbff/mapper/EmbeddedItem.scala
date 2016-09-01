package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.Mapping

case class EmbeddedItem[O <: Mapping](owner: Mapper[Any, O], param: Any) extends Embedded[O, Option[O]] {

  def params = List(param)

  def isValid = get.exists(_.isValid)

  private[bff] def isMaterialized = value.isDefined

  private[bff] var value: Option[Option[O]] = None

  private[bff] def materialize(values: Map[Any, O]) =
    value = Some(values.get(param))
}
