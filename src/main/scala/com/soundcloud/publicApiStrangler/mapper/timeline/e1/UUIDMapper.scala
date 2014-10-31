package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import java.nio.ByteBuffer
import java.util.UUID

import com.fasterxml.uuid.impl.UUIDUtil
import org.apache.commons.codec.binary.Base64

/*
 * The underlying systems don't use UUIDs, but the public-api clients do. We rely on random-base UUIDs and
 * construct them by hand in order to pass the cursor through. Ah, the joys of legacy.
 */
object UUIDMapper {

  def toCursor(uuid: Option[UUID]): Option[String] = {
    if(validUuid(uuid)) {
      Some(unpack(uuid.get))
    } else {
      None // invalid cursors
    }
  }

  def validUuid(uuid: Option[UUID]): Boolean = {
    uuid.isDefined && uuid.get.getMostSignificantBits > 0
  }

  def fromCursor(cursor: Option[String]): Option[UUID] = {
    cursor match {
      case Some(something) => Some(pack(something))
      case _ => None
    }
  }

  private val GokuCursor = """^(\d+)A(.*)""".r

  private def pack(cursor: String) = {
    val GokuCursor(timestamp, rest) = cursor
    val mostSig = timestamp.toLong
    val leastSig = ByteBuffer.wrap(Base64.decodeBase64(rest.getBytes("UTF-8"))).getLong
    UUIDUtil.uuid(ByteBuffer.allocate(16).putLong(mostSig).putLong(leastSig).array)
  }

  private def unpack(uuid: UUID) = {
    val rest = new String(
      Base64.encodeBase64(ByteBuffer.allocate(16).putLong(uuid.getLeastSignificantBits).array()),
      "UTF-8"
    ).trim

    s"${uuid.getMostSignificantBits}A${rest}"
  }
}
