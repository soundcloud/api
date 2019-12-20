package com.soundcloud.publicApiStrangler.authorization.policies

object InvalidPolicyException {
  val MESSAGE = "[%s] is not a valid %s, must be one of [%s]"
}

case class InvalidPolicyException(val invalidPolicyName: String)
    extends RuntimeException(
      String.format(
        InvalidPolicyException.MESSAGE,
        invalidPolicyName,
        classOf[ContentPolicy].getSimpleName,
        ContentPolicy.allPossibleNames
      )
    )

/**
  * The set of policies to be applied to a content.
  * <p/>
  * Policies inform in which context a piece content (e.g. a track) <i>SHOULD</i> be used.
  */
class ContentPolicy(val name: String) {
  override def toString: String = name

  def getPrintName: String = name
}

/**
  * The set of policies to be applied to a content.
  * <p/>
  * Policies inform in which context a piece content (e.g.a track) <i>
  * SHOULD
  * </i> be used.
  */
object ContentPolicy {

  /**
    * Can be used by the client and user.
    */
  case object ALLOW extends ContentPolicy("ALLOW")

  /**
    * Can be used only when monetization features are enabled.
    */
  case object MONETIZE extends ContentPolicy("MONETIZE")

  /**
    * Cannot be used or seen at all.
    */
  case object BLOCK extends ContentPolicy("BLOCK")

  /**
    * Only a preview ("snippet") of the content is available.
    * <p/>
    * e.g. the first 30 seconds of a track.
    */
  case object SNIP extends ContentPolicy("SNIP")

  val DEPRECATED_TO_CURRENT = Map(
    "ALLOWED" -> ALLOW.getPrintName,
    "SNIPPET" -> SNIP.getPrintName,
    "BLOCKED" -> BLOCK.getPrintName
  )

  def values = List(ALLOW, MONETIZE, BLOCK, SNIP)

  val allPossibleNames = Set(ALLOW, MONETIZE, BLOCK, SNIP).map(_.getPrintName)

  def sanitize(printName: String) = {
    val sanitized = printName.toUpperCase()
    DEPRECATED_TO_CURRENT.getOrElse(sanitized, sanitized)
  }

  def from(printName: String): ContentPolicy = {
    val sanitized = sanitize(printName)
    values.find(_.name == sanitized).getOrElse(throw new InvalidPolicyException(sanitized))
  }
}
