package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Self
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures

class SpotlightItemMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val spotlightItem: SpotlightItem
  }

  "#apply" >> {
    trait ApplyContext extends Context {
      override lazy val spotlightItem = SpotlightItemMapper(Fixtures.okidokiSpotlightItem)
    }

    "maps self" in new ApplyContext {
      spotlightItem.self ==== Self(Urn("soundcloud:playlists:6584580"), "http://moshimoshi.int.s-cloud.net/playlists/soundcloud:sets:6584580")
    }

    "maps user" in new ApplyContext {
      spotlightItem.user ==== Self(Urn("soundcloud:users:21778"), "http://moshimoshi.int.s-cloud.net/users/soundcloud:users:21778")
    }

    "maps public" in new ApplyContext {
      spotlightItem.public ==== true
    }

    "maps title" in new ApplyContext {
      spotlightItem.title ==== "Quarters"
    }

    "maps lastModified" in new ApplyContext {
      spotlightItem.lastModified ==== "2015/05/22 07:38:07 +0000"
    }
  }
}
