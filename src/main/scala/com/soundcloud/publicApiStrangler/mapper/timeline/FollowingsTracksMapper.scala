package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.TimelineWithUuids
import com.soundcloud.services.timeline.TimelineJsonClient
import com.twitter.util.Future

class FollowingsTracksMapper(timelineClient: TimelineJsonClient,
                             entityMapper: EntityMapper,
                             entitySummaryMapper: EntitySummaryMapper)
  extends TimelineMapper {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[TimelineWithUuids] = {
    val (uuid, reverse) = clientCursorParam(page)

    timelineClient.followingsTracks(session, uuid.map(_.toString), page.limit, reverse, Some("uuid")).map {
      json =>
        new TimelineWithUuids(json, page, entityMapper, entitySummaryMapper)
    }
  }
}
