package com.soundcloud.bff.nextbff.repository

import java.util.concurrent.TimeUnit

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.{Future, JsObject}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Await
import org.mockito.Mockito.verify

class UrnsCacheSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val cacheExpirationTime = 10
    val urn1 = Urn("some:urn:one")
    val urn2 = Urn("some:urn:two")
    val urns = List(urn1, urn2)

    val cache = mock[Cache]

    def bulkFetch(urns: Set[Urn]): Future[Map[Urn, JsObject]]

    def cacheAllowed(obj: JsObject) = false

    lazy val subject = new UrnsCache(cache)
    lazy val result =
      Await.result(subject.get(urns, bulkFetch, cacheExpirationTime, cacheAllowed)).mapValues(_.toString)
  }

  trait FullyCached extends Context {
    val jsonResult1 = "{\"id\":\"some:urn:one\"}"
    val jsonResult2 = "{\"id\":\"some:urn:two\"}"

    override def before = {
      when(cache.get(any[String]))
        .thenReturn(Future.value(Option(jsonResult1)))
        .thenReturn(Future.value(Option(jsonResult2)))
    }
  }

  "retrieves all Urns from cache" in new FullyCached {
    override def bulkFetch(urns: Set[Urn]) = ???

    result must be_==(Map(urn1 -> jsonResult1, urn2 -> jsonResult2))
  }

  trait PartiallyCached extends Context {
    val jsonResult1 = "{\"id\":\"some:urn:one\"}"
    val jsonResult2 = "{\"id\":\"some:urn:two\"}"

    override def before = {
      when(cache.get(any[String]))
        .thenReturn(Future.value(Option(jsonResult1)))
        .thenReturn(Future.value(None))
    }
  }

  "fetches the non-cached Urns" in new PartiallyCached {
    override def bulkFetch(urns: Set[Urn]) = {
      urns must be_==(List(urn2))
      Future.value(Map(urn2 -> Json.fromString(jsonResult2).as[JsObject]))
    }

    result must be_==(Map(urn1 -> jsonResult1, urn2 -> jsonResult2))
  }

  "ignores urns not found by the bulkFetch" in new PartiallyCached {
    override def bulkFetch(urns: Set[Urn]) =
      Future.value(Map.empty)

    result must be_==(Map(urn1 -> jsonResult1))
  }

  trait AddToCache extends PartiallyCached {
    override def cacheAllowed(obj: JsObject) = true

    override def bulkFetch(urns: Set[Urn]) =
      Future.value(Map(urn2 -> Json.fromString(jsonResult2).as[JsObject]))
  }

  "adds to cache allowed content" in new AddToCache {
    result must be_==(Map(urn1 -> jsonResult1, urn2 -> jsonResult2))

    verify(cache).set(
      "some:urn:two",
      jsonResult2,
      cacheExpirationTime,
      TimeUnit.MINUTES)
  }

  trait NothingCached extends Context {
    val jsonResult1 = "{\"id\":\"some:urn:one\"}"
    val jsonResult2 = "{\"id\":\"some:urn:two\"}"

    override def before = {
      when(cache.get(any[String]))
        .thenReturn(Future.value(None))
        .thenReturn(Future.value(None))
      super.before
    }
  }

  "fetches all the Urns" in new NothingCached {
    override def bulkFetch(urnsList: Set[Urn]) = {
      urnsList must be_==(urns)
      Future.value(Map(
        urn1 -> Json.fromString(jsonResult1).as[JsObject],
        urn2 -> Json.fromString(jsonResult2).as[JsObject]
      ))
    }

    result must be_==(Map(urn1 -> jsonResult1, urn2 -> jsonResult2))
  }
}
