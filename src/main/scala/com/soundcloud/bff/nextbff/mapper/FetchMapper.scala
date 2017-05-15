package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.nextbff.repository.BulkFetchRepository
import com.soundcloud.jvmkit.module.util.session.UserSession
import play.api.libs.json.JsValue

/**
 * Maps from an inp ut to output, where the output is a mapping object.
 * It uses the provided repository to fetch the json used to create the mapping.
 *
 * @tparam I Any input object.
 * @tparam O Mapping that will be rendered as json.
 */
trait FetchMapper[I, O <: Mapping] extends Mapper[I, O] {

  val repository: BulkFetchRepository[I]

  override def map(session: UserSession, inputs: Set[I])(implicit context: MappingContext) =
    repository.bulkFetch(session, inputs).map(_.map {
      case (input, json) =>
        input -> map(input, json)
    })

  def map(param: I, json: JsValue)(implicit context: MappingContext): O
}
