package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.TrackPurchaseLink
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class TrackPurchaseLinkMapperSpec extends UnitSpecification {
  "Maps a track" >> {

    "All fields" in {
      val presented = okidokiTrackPurchaseLinks.as[List[TrackPurchaseLink]].last

      presented.track_urn ==== Urn("soundcloud:tracks:2")
      presented.title ==== Some("ultratv")
      presented.url ==== "http://bit.ly/14XeNOe"
    }
  }

  "Minimal fields" in {
    val presented = okidokiTrackPurchaseLinks.as[List[TrackPurchaseLink]].head
    presented.title ==== None
  }
}
