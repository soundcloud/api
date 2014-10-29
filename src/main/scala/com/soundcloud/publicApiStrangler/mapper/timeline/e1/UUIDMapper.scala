package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import java.util.UUID

import com.fasterxml.uuid.UUIDType
import com.fasterxml.uuid.impl.UUIDUtil

/*
 * The underlying systems don't use UUIDs, but the public-api clients do. We rely on random-base UUIDs and
 * construct them by hand in order to pass the cursor through. Ah, the joys of legacy.
 */
object UUIDMapper {

  def toCursor(uuid: Option[UUID]): Option[String] = {
    if(validUuid(uuid)) {
      Some(s"${uuid.get.getMostSignificantBits}A00000000000000000000")
    } else {
      None // invalid cursors
    }
  }

  def validUuid(uuid: Option[UUID]): Boolean = {
    uuid.isDefined && uuid.get.getMostSignificantBits > 0
  }

  def fromCursor(cursor: Option[String]): Option[UUID] = {
    if(cursor.isDefined) {
      val parts = cursor.get.split("A")
      Some(UUIDUtil.constructUUID(UUIDType.RANDOM_BASED, parts.head.toLong, 0))
    } else {
      None
    }
  }

}
