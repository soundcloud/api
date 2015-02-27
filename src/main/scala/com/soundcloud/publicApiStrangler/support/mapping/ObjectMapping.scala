package com.soundcloud.publicApiStrangler.support.mapping

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}

abstract class ObjectMapping[T](res: T)(implicit context: MappingContext) extends Mapping {
  def resource = res
}
