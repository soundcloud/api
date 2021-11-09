package com.soundcloud.publicApiStrangler.authorization.policies

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ContentRestrictionSpec extends Specification with Mockito {
  "#contentRestriction" >> {
    "Content restriction must return NO_THIRD_PARTY_OFFLINE_SYNC" in new Scope {
      ContentRestriction.from("NO_THIRD_PARTY_OFFLINE_SYNC") mustEqual ContentRestriction.NO_THIRD_PARTY_OFFLINE_SYNC
    }
  }

  "#contentRestriction" >> {
    "Content restriction must return ENCRYPTED_STREAM_ONLY" in new Scope {
      ContentRestriction.from("ENCRYPTED_STREAM_ONLY") mustEqual ContentRestriction.ENCRYPTED_STREAM_ONLY
    }
  }

  "#contentRestriction" >> {
    "Content restriction must return NO_PROGRESSIVE_DOWNLOAD" in new Scope {
      ContentRestriction.from("NO_PROGRESSIVE_DOWNLOAD") mustEqual ContentRestriction.NO_PROGRESSIVE_DOWNLOAD
    }
  }

  "#contentRestriction" >> {
    "Content restriction must return NO_EXTERNAL_SHARE" in new Scope {
      ContentRestriction.from("NO_EXTERNAL_SHARE") mustEqual ContentRestriction.NO_EXTERNAL_SHARE
    }
  }

  "#contentRestriction" >> {
    "Content restriction must return NO_OFFLINE_SYNC" in new Scope {
      ContentRestriction.from("NO_OFFLINE_SYNC") mustEqual ContentRestriction.NO_OFFLINE_SYNC
    }
  }

  "#contentRestriction" >> {
    "Content restriction must a valid set of restrictions" in new Scope {
      val contentRestrictions = List(
        "NO_THIRD_PARTY_OFFLINE_SYNC",
        "ENCRYPTED_STREAM_ONLY",
        "NO_PROGRESSIVE_DOWNLOAD",
        "NO_EXTERNAL_SHARE",
        "NO_OFFLINE_SYNC"
      )
      ContentRestriction.from(contentRestrictions).toSet mustEqual ContentRestriction.values.toSet
    }
  }
}
