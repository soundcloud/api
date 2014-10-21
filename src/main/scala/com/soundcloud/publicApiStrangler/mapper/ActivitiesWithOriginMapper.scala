package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.TimelineWithOrigin
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.TimelineClient
import com.twitter.util.Future


class ActivitiesWithOriginMapper(timelineClient: TimelineClient,
                   entityMapper: EntityMapper,
                   entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineWithOrigin] = {
    // activities on public api are actually the user' STREAM. Go figure.
    timelineClient.stream(session, page.cursor, page.limit).map { json =>
      new TimelineWithOrigin(json, page, entityMapper, entitySummaryMapper)
    }
  }
}
