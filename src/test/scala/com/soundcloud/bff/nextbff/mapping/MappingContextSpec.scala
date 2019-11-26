package com.soundcloud.bff.nextbff.mapping

import com.soundcloud.bff.nextbff.mapper.{EmbeddedItem, Mapper}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class MappingContextSpec extends UnitSpecification {
  trait AttachContext extends Scope {
    val session = mock[UserSession]
    implicit val context = new MappingContext(session)
    val mapper = mock[Mapper[Any, Mapping]]
    val urn = Urn("soundcloud", "tracks", "22")
    val embedded = EmbeddedItem(mapper, urn)
  }

  "attaches equal items only one time" in new AttachContext {
    (context.attach(embedded) eq embedded) must beTrue
    (context.attach(EmbeddedItem(mapper, urn)) eq embedded) must beTrue
  }

  trait MaterializeContext extends AttachContext with Before {
    val mapping = new Mapping {}
    val mapResult = Map[Any, Mapping](urn -> mapping)

    override def before: Any = {
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
