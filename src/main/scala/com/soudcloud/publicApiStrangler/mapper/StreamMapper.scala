package com.soudcloud.publicApiStrangler.mapper

import com.soudcloud.publicApiStrangler.mapping.Timeline
import com.soundcloud.bff.nextbff.mapper.{Mapper, Page}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.{MoshimoshiClient, TimelineClient}
import com.twitter.util.Future

class StreamMapper(timelineClient: TimelineClient,
                   entityMapper: EntityMapper) extends ClientBasedMapper[CursorBasedPage[Urn], Timeline] {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[Timeline] = {
    timelineClient.stream(session, page.cursor, page.limit).map { json =>
      new Timeline(json, page, entityMapper)
    }
  }

}
