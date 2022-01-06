package com.soundcloud.apipublic.handler.support.requestParser

import com.soundcloud.apipublic.authorization.policies.Access
import com.twitter.finagle.http.ParamMap

object AccessParamsExtractor {

  val access: Map[String, Access] = Map(
    "playable" -> Access.Playable,
    "preview" -> Access.Preview,
    "blocked" -> Access.Blocked
  )

  def unapply(params: ParamMap, predefinedAccess: AccessParams = AccessParams.defaultAccess): AccessParams =
    params
      .get("access")
      .map(value => {
        AccessParams(value.split(',').flatMap(access.get).toSet) match {
          case AccessParams(access) if access.isEmpty => predefinedAccess
          case params => params
        }
      })
      .getOrElse(predefinedAccess)
}

case class AccessParams(access: Set[Access] = Set(Access.Preview, Access.Playable))

object AccessParams {
  val defaultAccess: AccessParams = AccessParams()
  val streamAccess: AccessParams = AccessParams(Set(Access.Playable, Access.Preview))
  val explicitAccess: AccessParams = AccessParams(Set(Access.Playable, Access.Preview, Access.Blocked))
}
