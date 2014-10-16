package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.TimelineV1
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.Future


class ActivitiesV1Mapper(timelineClient: TimelineClient,
                   entityMapper: EntityMapper,
                   entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineV1] = {
    timelineClient.activities(session, page.cursor, page.limit).map { json =>
      new TimelineV1(json, page, entityMapper, entitySummaryMapper)
    }
  }
}
