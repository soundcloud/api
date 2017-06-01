package com.soundcloud.publicApiStrangler.mapper.timeline

import java.util.UUID

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.module.http.client.StringParam
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.Timeline
import com.twitter.util.Future

trait TimelineMapper extends Mapper[CursorBasedPage[Urn], Timeline] {

  def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[Timeline]

  protected def clientCursorParam(page: CursorBasedPage[Urn]): (Option[UUID], Boolean) = {
    page.extraParams.get("uuid[to]") match {
      case Some(StringParam(uuid)) =>
        (Some(UUID.fromString(uuid)), true)
      case _ =>
        (page.cursor.map(UUID.fromString), false)
    }
  }

  override def map(session: UserSession,
                   inputs: Set[CursorBasedPage[Urn]])(implicit context: MappingContext): Future[Map[CursorBasedPage[Urn], Timeline]] = {
    Future.collect(
      inputs.toSeq.map { i =>
        fetch(session, i).map { o =>
          i -> o
        }
      }
    ).map(_.toMap)
  }

}
