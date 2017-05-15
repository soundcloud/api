package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.repository.JsonServiceRepository
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Matchers
import play.api.libs.json.Json
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class CachingByUrnSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    val service = smartMock[JsonService]
    val urnsCache = smartMock[UrnsCache]
    val session = smartMock[UserSession]
    val urns = Set(new Urn("soundcloud:users:321321"), new Urn("soundcloud:users:4444"))
    val cacheExpirationTimeMinutes = 1
    val jsObject = Json.obj()

    val repo = new JsonServiceRepository(service) with BulkFetchByUrnRepository {
      override def fetch(session: UserSession, urns: Set[Urn]) = ???
    }

    val subject = new CachingByUrn(repo, urnsCache, cacheExpirationTimeMinutes)

    override def before: Any = {
      when(urnsCache.get(Matchers.eq(urns.toList), Matchers.any(), Matchers.eq(cacheExpirationTimeMinutes), Matchers.any()))
        .thenReturn(Future.value(Map(urns.head -> jsObject)))
    }
  }

  "uses the urns cache to fetch" in new Context {
    Await.result(subject.bulkFetch(session, urns)) must be_==(Map(urns.head -> jsObject))
  }
}
