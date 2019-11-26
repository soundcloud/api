package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.Mapping

case class EmbeddedAttribute[O <: Mapping, A](owner: Mapper[Any, O], param: Any, f: O => A)
    extends Embedded[O, Option[A]] {
  def params = List(param)

  override def isValid = rawValue.get.exists(_.isValid)

  private[bff] def isMaterialized = rawValue.isDefined

  private var rawValue: Option[Option[O]] = None

  protected[bff] def value =
    rawValue.map(_.map(f))

  private[bff] def materialize(values: Map[Any, O]) =
    rawValue = Some(values.get(param))
}
