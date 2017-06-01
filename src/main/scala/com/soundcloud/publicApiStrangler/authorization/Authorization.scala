package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.policies.{ContentAuthorization, ContentPolicy, ContentRestriction, Reason}
import com.soundcloud.publicApiStrangler.representation.HasUrn
import play.api.libs.json.JsValue

/**
  * Holds the current [[ContentAuthorization]] for a specific piece of content.
  *
  * @param jsonContent          The JSON representation for the content. Must conform with [[HasUrn]]
  * @param contentAuthorization Authorization data for this piece of content
  * @param available            If the content is available to the request or not. Instead of querying this, please check
  *                             [[Available]] and [[Unavailable]] for pattern matching.
  */
sealed class Authorization(val jsonContent: JsValue, val contentAuthorization: ContentAuthorization, val available: Boolean) {
  val urn = HasUrn(jsonContent).orNull

  override def toString = s"${this.getClass.getSimpleName}{available='$available', " +
    s"policy='${contentAuthorization.getPolicy}', urn='$urn'}"
}

/**
  * The content is available to this request. [[ContentRestriction]] still apply.
  */
object Available {
  def apply(jsonContent: JsValue, contentAuthorization: ContentAuthorization) = {
    new Authorization(jsonContent, contentAuthorization, true)
  }

  def unapply(authorization: Authorization): Option[(JsValue, ContentPolicy, Set[ContentRestriction])] = {
    if (authorization.available) {
      Some(
        (authorization.jsonContent,
          authorization.contentAuthorization.getPolicy,
          authorization.contentAuthorization.getContentRestrictions.toSet)
      )
    } else {
      None
    }
  }
}

/**
  * The content is not available to this request under any conditions.
  */
object Unavailable {
  def apply(jsonContent: JsValue) = new Authorization(jsonContent, null, false)

  def apply(jsonContent: JsValue, contentAuthorization: ContentAuthorization) = new Authorization(jsonContent, contentAuthorization, false)

  def unapply(authorization: Authorization): Option[(Urn, Reason)] = Some((authorization.urn, authorization.contentAuthorization.getReason))
}
