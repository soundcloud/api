package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

trait InputValidation[I, O <: Mapping] {
  this: Mapper[I, O] =>

  override def map(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]] =
    inputs match {
      case empty if empty.isEmpty => Future.value(Map.empty)
      case nonEmpty => mapNonEmptyInputs(session, nonEmpty)
    }

  def mapNonEmptyInputs(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]]
}
