package com.soundcloud.publicApiStrangler.mapping

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.publicApiStrangler.mapper.{EntitySummaryMapper, EntityMapper}
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsValue

class Comment(json: JsValue,
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends CommentSummary(json, baseUrl, entitySummaryMapper) with UrnSupport {

  val track = entitySummaryMapper.embed(trackUrn)
  val user = entitySummaryMapper.embed(userUrn)

}
