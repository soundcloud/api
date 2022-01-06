package com.soundcloud.apipublic.authorization.policies

/**
  * All monetizable tracks (ads and paywall) will be surfaced with either {@link ContentPolicy#MONETIZE}
  * or {@link ContentPolicy#SNIP} and the <code>MonetizationModel</code> will be used by the BFFs and
  * clients to determine playback and display (for upsells, etc).
  */
class MonetizationModel(val name: String) {
  override def toString = name
}

object MonetizationModel {

  /**
    * Nothing to see here.
    */
  case object NOT_APPLICABLE extends MonetizationModel("NOT_APPLICABLE")

  /**
    * Track can have advertising played against it.
    */
  case object AD_SUPPORTED extends MonetizationModel("AD_SUPPORTED")

  /**
    * Track is available to mid-tier consumer subscribers.
    */
  case object SUB_MID_TIER extends MonetizationModel("SUB_MID_TIER")

  /**
    * Track is available to high-tier consumer subscribers.
    */
  case object SUB_HIGH_TIER extends MonetizationModel("SUB_HIGH_TIER")

  /**
    * Track can have advertising played against it when no policy is explicitly set or an allow policy is set.
    */
  case object BLACKBOX extends MonetizationModel("BLACKBOX")

  def from(n: String): MonetizationModel = {
    values.find(_.name == n).getOrElse(throw new IllegalArgumentException(s"No value for name $n"))
  }

  def values = List(NOT_APPLICABLE, AD_SUPPORTED, SUB_MID_TIER, SUB_HIGH_TIER, BLACKBOX)
}
