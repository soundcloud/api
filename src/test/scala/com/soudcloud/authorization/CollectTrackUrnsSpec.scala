package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.Urn

class CollectTrackUrnsSpec extends UnitSpecification with Fixtures {

  "ext‰ract urns from a valid json" >> {
    "single track" in {
      CollectTrackUrns(singleTrackJson.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual singleTrackJson
          urns mustEqual List(Urn("soundcloud:tracks:153896632"))
        case other =>
          ko
      }
    }

    "tracks array" in {
      CollectTrackUrns(tracksArrayJson.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual tracksArrayJson
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
      CollectTrackUrns(playlistJson.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual playlistJson
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
  }

  "extract urns from a valid XML" >> {
    "single track" in {
      CollectTrackUrns(singleTrackXml.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual singleTrackXml
          urns mustEqual List(Urn("soundcloud:tracks:153896632"))
        case other =>
          ko
      }
    }

    "tracks array" in {
      CollectTrackUrns(tracksArrayXml.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual tracksArrayXml
          urns mustEqual
            List(
              Urn("soundcloud:tracks:160943944"),
              Urn("soundcloud:tracks:160943940"),
              Urn("soundcloud:tracks:160943936"),
              Urn("soundcloud:tracks:160943935"),
              Urn("soundcloud:tracks:160943934"),
              Urn("soundcloud:tracks:160943933"),
              Urn("soundcloud:tracks:160943931"),
              Urn("soundcloud:tracks:160943930")
            )
        case other =>
          ko
      }
    }

    "playlist" in {
      CollectTrackUrns(playlistXml.toString) match {
        case Some((wrapper, urns)) =>
          wrapper.raw mustEqual playlistXml
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
              Urn("soundcloud:tracks:299"),
              Urn("soundcloud:tracks:300"))
        case other =>
          ko
      }
    }
  }

  "return empty for an invalid json" in {
    CollectTrackUrns("bad bad json") must beEmpty
  }

  "return empty if the json doesn't have tracks" in {
    CollectTrackUrns(userJson.toString) must beEmpty
  }
}
