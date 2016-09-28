package com.soundcloud.bff.nextbff.test

import com.soundcloud.bff.nextbff.repository.UrnsCache
import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.test.AlwaysMissCache
import play.api.libs.json.JsObject
import com.twitter.util.Future

class AlwaysMissUrnsCache extends UrnsCache(AlwaysMissCache) {

  override def get(urns: List[Urn],
                   bulkFetch: Set[Urn] => Future[Map[Urn, JsObject]],
                   cacheExpirationTimeMinutes: Int,
                   cacheAllowed: JsObject => Boolean) =
    bulkFetch(urns.toSet)
}
