package com.soundcloud.bff.nextbff.mapping

import com.soundcloud.bff.Future
import com.soundcloud.bff.nextbff.mapper.{EmbeddedItem, Mapper}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Await

class MappingContextSpec extends UnitSpecification {

  trait AttachContext extends VerifiedMocks {
    val session = mock[UserSession]
    implicit val context = new MappingContext(session)
    val mapper = mock[Mapper[Any, Mapping]]
    val urn = new Urn("soundcloud:tracks:22")
    val embedded = EmbeddedItem(mapper, urn)
  }

  "attaches equal items only one time" in new AttachContext {
    (context.attach(embedded) eq embedded) must beTrue
    (context.attach(EmbeddedItem(mapper, urn)) eq embedded) must beTrue
  }

  trait MaterializeContext extends AttachContext {
    val mapping = new Mapping {}
    val mapResult = Map[Any, Mapping](urn -> mapping)

    override def before = {
      when(mapper.map(session, Set(urn)))
        .thenReturn(Future(mapResult))

      context.attach(embedded)
    }
  }

  "materializes embeddeds" in new MaterializeContext {
    Await.result(context.materialize)
    embedded.get ==== Some(mapping)
  }
}
