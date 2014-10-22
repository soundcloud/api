package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, TimelineMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.TimelineWithUuids
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.StringParam
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.Future

class StreamMapper(timelineClient: TimelineClient,
                   entityMapper: EntityMapper,
                   entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineWithUuids] = {
    val (cursor, reverse) = page.extraParams.get("uuid[to]") match {
      case Some(StringParam(uuid)) => (Some(uuid), true)
      case _ => (page.cursor, false)
    }

    timelineClient.stream(session, cursor, page.limit, reverse).map { json =>
      new TimelineWithUuids(json, page, entityMapper, entitySummaryMapper)
    }
  }
}
