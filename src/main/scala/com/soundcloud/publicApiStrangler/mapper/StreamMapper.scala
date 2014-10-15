package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.mapping.Timeline
import com.soundcloud.bff.nextbff.mapper.{Mapper, Page}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.StringParam
import com.soundcloud.service.client.{MoshimoshiClient, TimelineClient}
import com.twitter.util.Future

class StreamMapper(timelineClient: TimelineClient,
                   entityMapper: EntityMapper) extends ClientBasedMapper[CursorBasedPage[Urn], Timeline] {

  override def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[Timeline] = {
    val (cursor, reverse) = page.extraParams.get("uuid[to]") match {
      case Some(StringParam(uuid)) => (Some(uuid), true)
      case _ => (page.cursor, false)
    }

    timelineClient.stream(session, cursor, page.limit, reverse).map { json =>
      new Timeline(json, page, entityMapper)
    }
  }
}
