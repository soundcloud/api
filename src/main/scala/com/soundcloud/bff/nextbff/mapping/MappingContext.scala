package com.soundcloud.bff.nextbff.mapping

import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.soundcloud.bff.Future
import com.soundcloud.bff.nextbff.mapper.{Embedded, Mapper}
import com.soundcloud.jvmkit.UserSession

import scala.collection.mutable.{Map => MutableMap, Set => MutableSet}


@JsonIgnoreType
class MappingContext(val session: UserSession) {

  private val materialized = MutableMap[Mapper[Any, Mapping], MutableMap[Any, Mapping]]()
  private val contents = MutableSet[Embedded[Mapping, Any]]()

  def attach[E <: Embedded[_, _]](item: E) = synchronized {
    contents.find(_ == item).getOrElse {
      contents += item.asInstanceOf[Embedded[Mapping, Any]]
      item
    }.asInstanceOf[E]
  }

  def materialize: Future[Unit] = synchronized {
    mostHeavyPedingItem match {
      case Some((mapper, items)) =>
        materialize(items.toSet, mapper.asInstanceOf[Mapper[Any, Mapping]])
          .flatMap(_ => materialize)
      case None =>
        Future.value(())
    }
  }

  private def materialize(items: Set[Embedded[Mapping, Any]], mapper: Mapper[Any, Mapping]) = {
    val mapperValues = materialized.getOrElseUpdate(mapper, MutableMap())
    val params = items.map(_.params).flatten -- mapperValues.keys
    mapper.map(session, params)(this).map {
      values =>
        mapperValues ++= values
        items.map(_.materialize(mapperValues.toMap))
    }
  }

  private def mostHeavyPedingItem =
    pending.groupBy(_.owner).toList.sortBy(_._1).headOption

  private def pending = contents.filterNot(_.isMaterialized)
}

object MappingContext {
  def materialize[O](session: UserSession)(f: MappingContext => O) = {
    val context = new MappingContext(session)
    val res = f(context)
    context.materialize.map(_ => res)
  }
}
