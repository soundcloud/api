package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.representation.{SimilarSounds, SimilarSoundsMeta}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class SimilarSoundsMapperSpec extends UnitSpecification {

  "maps similar sounds response to objects" in {
    val expectedTracks = Seq(
      Urn("soundcloud:tracks:139565597"),
      Urn("soundcloud:tracks:113100217"),
      Urn("soundcloud:tracks:93685166")
    )

    val expectedMeta = SimilarSoundsMeta(
      page = 1,
      pageSize = 3,
      variant = "default",
      sourceVersion = "snap-source",
      queryUrn = Urn("soundcloud:similarsounds:c90098b750d4470cafa834fb951fe657"),
      previousHref = "",
      nextHref = "https://similar-sounds.int.s-cloud.net/similar-to/soundcloud:tracks:125050457?page=2&page_size=3&query_urn=soundcloud%3Asimilarsounds%3Ac90098b750d4470cafa834fb951fe657&variant=default"
    )
    SimilarSoundsMapper(similarSoundsNonEmpty) ==== SimilarSounds(expectedTracks, expectedMeta)
  }

}
