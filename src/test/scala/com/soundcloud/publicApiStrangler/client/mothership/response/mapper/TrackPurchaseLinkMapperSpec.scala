package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.TrackPurchaseLink
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class TrackPurchaseLinkMapperSpec extends UnitSpecification {
  "Maps a track" >> {

    "All fields" in {
      val presented = okidokiTrackPurchaseLinks.as[List[TrackPurchaseLink]].last

      presented.track_urn ==== Urn("soundcloud", "tracks", "2")
      presented.title ==== Some("ultratv")
      presented.url ==== "http://bit.ly/14XeNOe"
    }
  }

  "Minimal fields" in {
    val presented = okidokiTrackPurchaseLinks.as[List[TrackPurchaseLink]].head
    presented.title ==== None
  }
}
