package com.soundcloud.publicApiStrangler.authorization.policies

sealed abstract class Access(val name: String) {
  override def toString: String = name
}

/**
  * The set of user access levels.
  * Determines what a user can do with a specific track
  */
object Access {

  /**
    * User is allowed to listen to a full track.
    */
  case object Playable extends Access("playable")

  /**
    * User is allowed to preview a track, meaning a snippet is available
    */
  case object Preview extends Access("preview")

  /**
    * User can only see the metadata of a track, no streaming is possible
    */
  case object Blocked extends Access("blocked")
}
