package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class Comment(jsonValue: JsValue,
               baseUrl: String,
               entitySummaryMapper: EntitySummaryMapper)(implicit context: MappingContext)
  extends CommentSummary(jsonValue, baseUrl, entitySummaryMapper) with UrnSupport {

  val track = entitySummaryMapper.embed(trackUrn)
  val user = entitySummaryMapper.embed(userUrn)

}
