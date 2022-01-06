package com.soundcloud.apipublic.authorization.policies

class ContentRestriction(val name: String) {

  /**
    * Returns the string representation of the enum value
    */
  def printName = name

  override def toString: String = name
}

/**
  * Set of restrictions a client application <i>MUST</i> enforce.
  */
object ContentRestriction {

  /**
    * The content is only available over encrypted streaming
    * (e.g. <a href=http://en.wikipedia.org/wiki/HTTP_Live_Streaming>HLS</a>). Non-encrypted media streams (e.g. MP3)
    * <i>MUST NOT</i> be made available to the user.
    */
  case object ENCRYPTED_STREAM_ONLY extends ContentRestriction("ENCRYPTED_STREAM_ONLY")

  /**
    * The content owner has chosen to not make it available for offline sync.
    */
  case object NO_OFFLINE_SYNC extends ContentRestriction("NO_OFFLINE_SYNC")

  /**
    * Restricts the track from being downloaded for offline sync on third party app.
    */
  case object NO_THIRD_PARTY_OFFLINE_SYNC extends ContentRestriction("NO_THIRD_PARTY_OFFLINE_SYNC")

  /**
    * The content owner has chosen to not make progressive download streaming available.
    */
  case object NO_PROGRESSIVE_DOWNLOAD extends ContentRestriction("NO_PROGRESSIVE_DOWNLOAD")

  /**
    * The content cannot be shared to an external platform.
    */
  case object NO_EXTERNAL_SHARE extends ContentRestriction("NO_EXTERNAL_SHARE")

  /**
    * Converts List of Strings into List of {@link ContentRestriction} instances.
    */
  def from(stringRestrictions: List[String]): List[ContentRestriction] =
    stringRestrictions.map(from(_))

  /**
    * Converts String into {@link ContentRestriction} instance.
    */
  def from(n: String): ContentRestriction = {
    values.find(_.name == n).getOrElse(throw new IllegalArgumentException(s"No value for name $n"))
  }

  def values =
    List(
      ENCRYPTED_STREAM_ONLY,
      NO_OFFLINE_SYNC,
      NO_THIRD_PARTY_OFFLINE_SYNC,
      NO_PROGRESSIVE_DOWNLOAD,
      NO_EXTERNAL_SHARE
    )

  /**
    * Returns string set of all possible string-representations of the enum
    */
  def allPossibleNames = values.map(_.name).toSet
}
