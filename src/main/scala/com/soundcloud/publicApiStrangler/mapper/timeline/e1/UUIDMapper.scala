package com.soundcloud.publicApiStrangler.mapper.timeline.e1

import java.util.UUID

import com.fasterxml.uuid.UUIDType
import com.fasterxml.uuid.impl.UUIDUtil

/*
 * The underlying systems don't use UUIDs, but the public-api clients do. We rely on random-base UUIDs and
 * construct them by hand in order to pass the cursor through. Ah, the joys of legacy.
 */
object UUIDMapper {

  def toCursor(uuid: UUID): String = s"${uuid.getMostSignificantBits}A00000000000000000000"

  def fromCursor(cursor: String): UUID = {
    val gokuTimestamp = cursor.split("A").head
    UUIDUtil.constructUUID(UUIDType.RANDOM_BASED, gokuTimestamp.toLong, 0)
  }

}
