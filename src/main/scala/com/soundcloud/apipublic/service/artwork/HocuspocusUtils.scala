package com.soundcloud.apipublic.service.artwork

import com.google.protobuf.ByteString
import com.soundcloud.hocuspocus.{Kind, Raw}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.io.Buf

object HocuspocusUtils {
  def toRaw(imageData: Buf, session: Option[UserSession]): Raw =
    Raw(Kind.ARTWORKS, ByteString.copyFrom(Buf.ByteArray.Owned.extract(imageData)), session.map(_.asProtoSession))
  def s3UrlRegex = "s3://([^/ ]+)/([^/ ]+)".r
}
