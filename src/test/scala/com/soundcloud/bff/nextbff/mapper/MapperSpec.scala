package com.soundcloud.bff.nextbff.mapper

import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class MapperSpec extends UnitSpecification {

  trait EmbeddedContext extends Scope {
    val session = mock[UserSession]
    implicit val context = new MappingContext(session)

    class TestMapping extends Mapping {
      val test = "testString"
    }

    val mapper = new Mapper[Any, TestMapping] {
      def map(session: UserSession, inputs: Set[Any])(implicit context: MappingContext) = ???
    }
  }

  trait EmbeddedItemContext extends EmbeddedContext {
    val urn = new Urn("soundcloud:users:333")

    val embedded = EmbeddedItem(mapper, urn)
  }

  "creates embedded items" in new EmbeddedItemContext {
    val result = mapper.embed(urn)
    result ==== embedded
    result.param ==== urn
  }

  trait EmbeddedAttributeContext extends EmbeddedContext {
    val urn = new Urn("soundcloud:users:333")
    val extractor = {
      output: TestMapping => output.test
    }
    val embeddedAttribute = EmbeddedAttribute(mapper, urn, extractor)
  }

  "creates embedded items" in new EmbeddedAttributeContext {
    val result = mapper.embedAttr(urn, extractor)
    result ==== embeddedAttribute
    result.param ==== urn
  }

  trait EmbeddedListContext extends EmbeddedContext {
    val urn1 = new Urn("soundcloud:users:333")
    val urn2 = new Urn("soundcloud:users:223")
    val urns = List(urn1, urn2)

    val embedded = EmbeddedList(mapper, urns)
  }

  "creates embedded lists" in new EmbeddedListContext {
    val result = mapper.embed(urns)
    result ==== embedded
    result.params ==== urns
  }

  trait MaterializeContext extends Scope {
    val session = mock[UserSession]
    implicit val context = new MappingContext(session)

    trait MapFunction {
      def map(session: UserSession, inputs: Set[Any]): Future[Map[Any, Mapping]]
    }

    val mapMock = mock[MapFunction]
    val mapResult: Map[Any, Mapping]

    val mapper = new Mapper[Any, Mapping] {
      def map(session: UserSession, inputs: Set[Any])(implicit context: MappingContext) =
        mapMock.map(session, inputs)
    }
  }

  trait MaterializeItemContext extends MaterializeContext with Before {

    val input = new Urn("soundcloud:users:333")
    val mapResult = Map[Any, Mapping](input -> new Mapping {})

    override def before: Any = {
      when(mapMock.map(session, Set(input))).thenReturn(Future(mapResult))
    }
  }

  "materializes items" in new MaterializeItemContext {
    Await.result(mapper.materialize(session, input)) mustEqual
      mapResult.values.headOption
  }

  trait MaterializeListContext extends MaterializeContext with Before {

    val urn1 = new Urn("soundcloud:users:333")
    val urn2 = new Urn("soundcloud:users:222")
    val inputs = List(urn1, urn2)

    val mapResult = Map[Any, Mapping](urn1 -> new Mapping {}, urn2 -> new Mapping {})

    override def before: Any = {
      when(mapMock.map(session, inputs.toSet)).thenReturn(Future(mapResult))
    }
  }

  "materializes lists" in new MaterializeItemContext {
    Await.result(mapper.materialize(session, input)) mustEqual
      mapResult.values.headOption
  }

  "Mapper[...]#filter" >> {
    "should yield a mapper that filters out items as per the additional constraint specified" in new MaterializeItemContext {

      case class Welp(s: String) extends Mapping

      val originalMapper = new Mapper[Int, Welp] {
        override def map(session: UserSession, inputs: Set[Int])(implicit context: MappingContext): Future[Map[Int, Welp]] = Future.value {
          inputs.zip(Seq("ein", "zwei", "hundert").map(Welp)).toMap
        }
      }
      val newMapper = originalMapper.filter(_.s.length <= 4)
      Await.result(newMapper.materialize(session, List(7, 0, 11))) ==== List(Welp("ein"), Welp("zwei"))
    }
  }
}
