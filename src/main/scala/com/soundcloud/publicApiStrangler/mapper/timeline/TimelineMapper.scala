package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.CursorBasedPage
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.timeline.Timeline
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future

trait TimelineMapper extends Mapper[CursorBasedPage[Urn], Timeline] {

  def fetch(session: UserSession, page: CursorBasedPage[Urn])(implicit context: MappingContext): Future[Timeline]

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
