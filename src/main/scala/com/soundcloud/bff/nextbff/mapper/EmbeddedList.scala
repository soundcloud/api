package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.Mapping

case class EmbeddedList[O <: Mapping](owner: Mapper[Any, O], params: List[Any]) extends Embedded[O, List[O]] {

  override def isValid = get.filter(_.isValid).nonEmpty

  def size = get.size

  private[bff] def isMaterialized = value.isDefined

  private[bff] var value: Option[List[O]] = None

  private[bff] def materialize(values: Map[Any, O]) =
    value = Some(params.map(values.get).flatten)
}
