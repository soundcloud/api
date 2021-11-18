package com.soundcloud.publicApiStrangler.service.artwork

import com.google.protobuf.ByteString
import com.soundcloud.hocuspocus.{Kind, Raw}
import com.twitter.io.Buf

object HocuspocusUtils {
  def toRaw(imageData: Buf): Raw = Raw(Kind.ARTWORKS, ByteString.copyFrom(Buf.ByteArray.Owned.extract(imageData)))
  def s3UrlRegex = "s3://([^/ ]+)/([^/ ]+)".r
}
