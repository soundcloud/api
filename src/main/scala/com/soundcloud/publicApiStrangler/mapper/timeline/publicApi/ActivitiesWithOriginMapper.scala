package com.soundcloud.publicApiStrangler.mapper.timeline.publicApi

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper, TimelineMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.publicApi.TimelineWithOrigin
import com.soundcloud.services.timeline.TimelineJsonClient
import com.twitter.util.Future


class ActivitiesWithOriginMapper(timelineClient: TimelineJsonClient,
                                 entityMapper: EntityMapper,
                                 entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineWithOrigin] = {
    val (uuid, reverse) = clientCursorParam(page)

    // activities on public api are actually the user' STREAM. Go figure.
    timelineClient.stream(session, uuid.map(_.toString), page.limit, reverse, Some("uuid")).map {
      json =>
        new TimelineWithOrigin(json, page, entityMapper, entitySummaryMapper)
    }
  }
}
