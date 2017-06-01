package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.TimelineJsonClient
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, TimelineMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.TimelineWithUuids
import com.twitter.util.Future

class ActivitiesMapper(timelineClient: TimelineJsonClient,
                       entityMapper: EntityMapper,
                       entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineWithUuids] = {
    val (uuid, reverse) = clientCursorParam(page)

    timelineClient.activities(session, uuid.map(_.toString), page.limit, reverse, Some("uuid")).map {
      json =>
        new TimelineWithUuids(json, page, entityMapper, entitySummaryMapper)
    }
  }

}
