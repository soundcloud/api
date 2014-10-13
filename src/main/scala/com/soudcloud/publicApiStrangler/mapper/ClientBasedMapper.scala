package com.soudcloud.publicApiStrangler.mapper

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.twitter.util.Future

trait ClientBasedMapper[I <: Comparable[I], O <: Mapping] extends Mapper[I, O] {

  def fetch(session: UserSession, page: I)(implicit context: MappingContext): Future[O]

  override def map(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]] = {
    Future.collect(
      inputs.toSeq.map { i =>
        fetch(session, i).map { o =>
          i -> o
        }
      }
    ).map(_.toMap)
  }

}
