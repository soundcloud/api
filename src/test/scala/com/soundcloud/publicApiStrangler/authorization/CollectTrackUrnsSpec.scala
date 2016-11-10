package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.jvmkit.Urn

class CollectTrackUrnsSpec extends UnitSpecification with Fixtures {

  "extract urns from json" >> {
    "single track" in {
      CollectTrackUrns(singleTrack.toString) match {
        case Some((visitor, urns)) =>
          visitor.wrapped mustEqual singleTrack
          urns mustEqual List(Urn("soundcloud:tracks:153896632"))
        case other =>
          ko
      }
    }

    "tracks array" in {
      CollectTrackUrns(tracksArray.toString) match {
        case Some((visitor, urns)) =>
          visitor.wrapped mustEqual tracksArray
          urns mustEqual
            List(
              Urn("soundcloud:tracks:49438146"),
              Urn("soundcloud:tracks:49437906"),
              Urn("soundcloud:tracks:48031525"))
        case other =>
          ko
      }
    }

    "playlist" in {
      CollectTrackUrns(playlist.toString) match {
        case Some((visitor, urns)) =>
          visitor.wrapped mustEqual playlist
          urns mustEqual
            List(
              Urn("soundcloud:tracks:290"),
              Urn("soundcloud:tracks:291"),
              Urn("soundcloud:tracks:292"),
              Urn("soundcloud:tracks:293"),
              Urn("soundcloud:tracks:294"),
              Urn("soundcloud:tracks:295"),
              Urn("soundcloud:tracks:296"),
              Urn("soundcloud:tracks:297"),
              Urn("soundcloud:tracks:298"),
              Urn("soundcloud:tracks:299"))
        case other =>
          ko
      }
    }

    "invalid json" in {
      CollectTrackUrns("bad bad json") must beEmpty
    }

    "json without tracks" in {
      CollectTrackUrns(user.toString) must beEmpty
    }
  }
}
