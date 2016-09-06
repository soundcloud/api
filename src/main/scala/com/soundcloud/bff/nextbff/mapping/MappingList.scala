package com.soundcloud.bff.nextbff.mapping

import com.fasterxml.jackson.annotation.JsonValue
import com.soundcloud.bff.nextbff.mapper.{Embedded, EmbeddedList}

case class MappingList[M <: Mapping] private[bff](embedded: EmbeddedList[M], filterIfDefined: Option[M => Embedded[_, _]] = None) {

  def size = embedded.params.size

  def filterIfDefined(f: M => Embedded[_, _]) =
    copy(filterIfDefined = Some(f))

  @JsonValue private def value =
    filterIfDefined.map {
      filter =>
        embedded.get.filter(filter(_).isValid)
    }.getOrElse(embedded)
}
