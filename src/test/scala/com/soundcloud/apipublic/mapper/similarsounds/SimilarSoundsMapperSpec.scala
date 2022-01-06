package com.soundcloud.apipublic.mapper.similarsounds

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures.similarSoundsNonEmpty

class SimilarSoundsMapperSpec extends UnitSpecification {

  "maps similar sounds response to objects" in {
    val expectedTracks = Seq(
      Urn("soundcloud", "tracks", "139565597"),
      Urn("soundcloud", "tracks", "113100217"),
      Urn("soundcloud", "tracks", "93685166")
    )

    val expectedMeta = SimilarSoundsMeta(
      pageSize = 50,
      variant = "default",
      sourceVersion = "snap-source",
      queryUrn = Urn("soundcloud", "similarsounds", "c90098b750d4470cafa834fb951fe657")
    )
    SimilarSoundsMapper(similarSoundsNonEmpty) ==== SimilarSounds(expectedTracks, expectedMeta)
  }
}
