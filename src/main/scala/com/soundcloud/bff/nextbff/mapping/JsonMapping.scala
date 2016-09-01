package com.soundcloud.bff.nextbff.mapping

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import play.api.libs.json.JsValue

@JsonIgnoreProperties(Array("json"))
abstract class JsonMapping(val json: JsValue)(implicit context: MappingContext) extends Mapping
