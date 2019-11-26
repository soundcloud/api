package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.moshiTrackGeoblockings

class GeoblockingsMapperSpec extends UnitSpecification {
  "maps attributes to object" in {
    val geoblockings = GeoblockingMapper(moshiTrackGeoblockings)

    geoblockings.get mustEqual (List("DE", "US"))
  }
}
