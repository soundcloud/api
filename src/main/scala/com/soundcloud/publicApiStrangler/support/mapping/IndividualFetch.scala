package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.twitter.util.Future

trait IndividualFetch[I, O <: Mapping] {
  this: InputValidation[I, O] =>

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]] =
    Future.collect(inputs.map {
      input =>
        mapSingleInput(session, input).map(input -> _)
    }.toList).map(_.toMap)


  def mapSingleInput(session: UserSession, input: I)(implicit context: MappingContext): Future[O]
}