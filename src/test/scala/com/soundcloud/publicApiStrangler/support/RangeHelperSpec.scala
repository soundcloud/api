package com.soundcloud.publicApiStrangler.support

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class RangeHelperSpec extends UnitSpecification {

  "#isRequestingFirstByte" >> {
    "it returns false when the range does not start with 0" in new Scope {
      RangeHelper.isRequestingFirstByte("bytes=200-399") ==== false
    }

    "it returns false when any of the ranges does not start at 0" in new Scope {
      RangeHelper.isRequestingFirstByte("bytes=200-399,-1") ==== false
    }

    "it returns true when the range starts at 0" in new Scope {
      RangeHelper.isRequestingFirstByte("bytes=0-100") ==== true
    }

    "it returns true when any of the ranges starts at 0" in new Scope {
      RangeHelper.isRequestingFirstByte("bytes=200-399,-1,0-1") ==== true
    }

    "it returns true with empty values" in new Scope {
      RangeHelper.isRequestingFirstByte("") ==== true
    }

    "it returns true with malformed values" in new Scope {
      RangeHelper.isRequestingFirstByte("=0-199") ==== true
    }

    "it returns true with unsupported types" in new Scope {
      RangeHelper.isRequestingFirstByte("bottles-of-beer=1-100") ==== true
    }
  }
}
