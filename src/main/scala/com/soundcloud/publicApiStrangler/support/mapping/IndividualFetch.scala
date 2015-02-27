package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff._
import com.soundcloud.bff.nextbff.mapping.{MappingContext, Mapping}
import com.soundcloud.jvmkit.UserSession

trait IndividualFetch[I, O <: Mapping] {
  this: InputValidation[I, O] =>

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]] =
    Future.collect(inputs.map {
      input =>
        mapSingleInput(session, input).map(input -> _)
    }.toList).map(_.toMap)


  def mapSingleInput(session: UserSession, input: I)(implicit context: MappingContext): Future[O]
}