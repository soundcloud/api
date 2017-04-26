package com.soundcloud.bff.nextbff.mapper

import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

@JsonIgnoreType
trait Mapper[I, O <: Mapping] { outer =>

  def embed(param: I)(implicit context: MappingContext) =
    context.attach(EmbeddedItem[O](this.asInstanceOf[Mapper[Any, O]], param))

  def embedAttr[A](param: I, extractor: O => A)(implicit context: MappingContext) =
    context.attach(EmbeddedAttribute[O, A](this.asInstanceOf[Mapper[Any, O]], param, extractor))

  def embed(params: List[I])(implicit context: MappingContext) =
    context.attach(EmbeddedList[O](this.asInstanceOf[Mapper[Any, O]], params))

  def materialize(session: UserSession, param: I) =
    MappingContext.materialize(session)(embed(param)(_)).map(_.get.filter(_.isValid))

  def materialize(session: UserSession, params: List[I]) =
    MappingContext.materialize(session)(embed(params)(_)).map(_.get.filter(_.isValid))

  def map(session: UserSession, inputs: Set[I])(implicit context: MappingContext): Future[Map[I, O]]

  private val weight =
    this.getClass.getDeclaredFields.count {
      field =>
        classOf[Mapper[_, _]].isAssignableFrom(field.getType)
    }

  def filter(condition: O => Boolean) = new Mapper[I, O] {
    override def map(session: UserSession, inputs: Set[I])
                    (implicit context: MappingContext): Future[Map[I, O]] = {
      outer.map(session, inputs)(context).map { m =>
        m.filter { case (_, v) => condition(v)}
      }
    }
  }
}

object Mapper {

  implicit val ordering = new Ordering[Mapper[Any, Mapping]] {
    def compare(x: Mapper[Any, Mapping], y: Mapper[Any, Mapping]) =
      y.weight - x.weight
  }
}
