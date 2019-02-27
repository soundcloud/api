package com.soundcloud.publicApiStrangler.support

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import com.soundcloud.jvmkit.module.util.Urn

class UrnSerializer extends JsonSerializer[Urn] {

  override def serialize(value: Urn, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeString(value.toString)
  }
}
