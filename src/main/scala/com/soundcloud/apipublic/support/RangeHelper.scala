package com.soundcloud.apipublic.support

object RangeHelper {
  val regex = """(?:(\w+)=|,)(\d*)-(\d*)""".r

  def isRequestingFirstByte(range: String): Boolean = {
    val matches = regex.findAllIn(range).matchData.toList

    val usingBytes = matches.headOption.exists(m => m.group(1) == "bytes")
    val requestingFirstByte = matches.exists(m => m.group(2) == "0")

    if (usingBytes && !requestingFirstByte) false
    else true
  }
}
