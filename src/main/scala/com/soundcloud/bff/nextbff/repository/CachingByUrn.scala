package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff._
import com.soundcloud.scalakit.{Urn, UserSession}

object CachingByUrn {
  private val defaultExpirationTime = 60
}

class CachingByUrn(repo: BulkFetchByUrnRepository, urnsCache: UrnsCache,
                   cacheExpirationTimeMinutes: Int = CachingByUrn.defaultExpirationTime) {

  def bulkFetch(session: UserSession, urns: Set[Urn]) =
    urnsCache.get(
      urns.toList,
      repo.bulkFetch(session, _),
      cacheExpirationTimeMinutes,
      cacheAllowed)

  private def cacheAllowed(obj: JsObject) =
    (obj \ "public").as[Option[Boolean]].getOrElse(true)
}
