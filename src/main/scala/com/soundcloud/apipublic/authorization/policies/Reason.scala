package com.soundcloud.apipublic.authorization.policies

/**
  * <a href=https://github.com/soundcloud/authsy>Authsy</a>'s best guest as of why a piece of content has a particular
  * set of {@link ContentPolicy} and
  * {@link ContentRestriction} applied to it.
  * <br><br>
  * <p>The understanding is:</p>
  *
  * When the result comes from a user policy = Reason.USER<br>
  * When the result comes from a client application policy = Reason.CLIENT_APPLICATION<br>
  * When the result comes from a geo policy = Reason.GEO<br>
  * When the result comes from the global default = Reason.DEFAULT<br>
  *
  */
class Reason(val name: String) {
  override def toString: String = name
}

object Reason {

  /**
    * It is not known why the policies and restrictions exist.
    */
  case object UNKNOWN extends Reason("UNKNOWN")

  /**
    * The policies and/or restrictions are bound to the geographic region where the
    * {@link com.soundcloud.jvmkit.UserSession} is coming from.
    */
  case object GEO extends Reason("GEO")

  /**
    * The rightsholder has decided that the content won't be available for this {@link com.soundcloud.jvmkit.UserSession}.
    * This often means the content is available only to allowlisted client applications and/or user accounts.
    */
  case object RIGHTSHOLDER_RESTRICTED extends Reason("RIGHTSHOLDER_RESTRICTED")

  /**
    * The policies/restrictions are coming from a default value.
    * This often means the content does not have a policy/restriction set for this {@link com.soundcloud.jvmkit.UserSession}.
    */
  case object DEFAULT extends Reason("DEFAULT")

  /**
    * The policies and/or restrictions are bound to the user present in this {@link com.soundcloud.jvmkit.UserSession}.
    */
  case object USER extends Reason("USER")

  /**
    * The policies and/or restrictions are bound to the client application present in this {@link com.soundcloud.jvmkit.UserSession}.
    */
  case object CLIENT_APPLICATION extends Reason("CLIENT_APPLICATION")

  /**
    * The application used by this {@link com.soundcloud.jvmkit.UserSession} is known not to support mandatory
    * conditions for this piece of content (e.g. it isn' monetisable and the content is only available to monetisable
    * platforms, or it doesn' support encrypted stream and this is required.
    * <p/>
    * Notice that Authsy cannot verify all possible platforms and restrictions, so this is only available for the most
    * common conditions.
    */
  case object NOT_SUPPORTED extends Reason("NOT_SUPPORTED")

  def from(n: String): Reason = {
    values.find(_.name == n).getOrElse(throw new IllegalArgumentException(s"No value for name $n"))
  }

  def values = List(UNKNOWN, GEO, RIGHTSHOLDER_RESTRICTED, DEFAULT, USER, CLIENT_APPLICATION, NOT_SUPPORTED)
}
