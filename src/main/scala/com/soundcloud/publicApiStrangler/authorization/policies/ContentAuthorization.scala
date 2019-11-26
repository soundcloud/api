package com.soundcloud.publicApiStrangler.authorization.policies

import com.soundcloud.jvmkit.module.util.Urn
import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder, ToStringBuilder, ToStringStyle}

/**
  * Describes in which conditions a request can access a given piece of content (e.g. track, playlist).
  * <p/>
  * The request is identified by a {@link com.soundcloud.jvmkit.UserSession} owned by
  * <a href=https://github.com/soundcloud/authenticator>Authenticator</a>. Based on the
  * <a href=http://eng-doc.int.s-cloud.net/guidelines/authorization/>authorization</a> data contained in this object,
  * <a href=https://github.com/soundcloud/authsy>Authsy</a> decides which
  * {@link ContentPolicy} and {@link ContentRestriction}
  * must be applied. Authsy <i>MAY</i> also set the {@link Reason} for such policies and
  * restrictions.
  * <p/>
  * <a href=https://github.com/soundcloud/bff>BFF applications</a> <i>MUST</i> follow the policies and restrictions.
  */
class ContentAuthorization(
    val urn: Urn,
    val policy: ContentPolicy,
    val reason: Reason,
    val contentRestrictions: Set[ContentRestriction],
    val monetizationModel: MonetizationModel
) {
  def this(urn: Urn, policy: ContentPolicy, reason: Reason, monetizationModel: MonetizationModel) {
    this(urn, policy, reason, Set[ContentRestriction](), monetizationModel)
  }

  def this(
      urn: Urn,
      policy: ContentPolicy,
      reason: Reason,
      contentRestriction: ContentRestriction,
      monetizationModel: MonetizationModel
  ) {
    this(urn, policy, reason, Set(contentRestriction), monetizationModel)
  }

  /**
    * The content these rules should apply to.
    */
  def getUrn: Urn = urn

  /**
    * The restrictions that <i>MUST</i> be applied when using the content.
    */
  def getContentRestrictions: Set[ContentRestriction] = contentRestrictions

  /**
    * The reason why such {@link ContentPolicy} and {@link ContentRestriction} are applied.
    * <p/>
    * This supplied in a <i>best-effort</i> basis, the reason reported here <i>MAY NOT</i> be the only reason a
    * particular set of rules are applied. This value is meant to be available to users, translated into some
    * user-friendly form.
    */
  def getReason: Reason = reason

  /**
    * The policy under such piece of content should be used.
    */
  def getPolicy: ContentPolicy = policy

  /**
    * The monetization model to apply to this piece of content.
    */
  def getMonetizationModel: MonetizationModel = monetizationModel

  override def toString: String =
    new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("urn", urn)
      .append("policy", policy)
      .append("monetizationModel", monetizationModel)
      .append("contentRestrictions", contentRestrictions)
      .append("reason", reason)
      .toString

  override def equals(o: Any): Boolean = {
    if (o.isInstanceOf[ContentAuthorization]) {
      val that = o.asInstanceOf[ContentAuthorization]
      return new EqualsBuilder()
        .append(urn, that.urn)
        .append(policy, that.policy)
        .append(monetizationModel, that.monetizationModel)
        .append(contentRestrictions, that.contentRestrictions)
        .append(reason, that.reason)
        .isEquals
    }
    false
  }

  override def hashCode: Int =
    new HashCodeBuilder()
      .append(urn)
      .append(policy)
      .append(monetizationModel)
      .append(contentRestrictions)
      .append(reason)
      .toHashCode
}
