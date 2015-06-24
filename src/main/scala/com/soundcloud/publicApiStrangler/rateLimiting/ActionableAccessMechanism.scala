package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.FailsafeUserSession
import com.soundcloud.ratelimiting.core.ClientApplication
import com.soundcloud.ratelimiting.core.RateLimitConfiguration.Bucket
import com.soundcloud.scalakit._

sealed trait ActionableAccessMechanism {
  import ActionableAccessMechanism._

  def clientApplication: ClientApplication

  final def bucket: Bucket = this match {
    case _: Anonymous => Bucket.ByClient
    case _: LoggedIn  => Bucket.ByUser
  }
}

object ActionableAccessMechanism {
  case class Anonymous(clientApplication: ClientApplication) extends ActionableAccessMechanism
  case class LoggedIn(clientApplication: ClientApplication, user: Urn) extends ActionableAccessMechanism

  def fromSession(session: UserSession): Option[ActionableAccessMechanism] = {
    (session, session.getAgent, Option(session.getUser)) match {
      case (_: FailsafeUserSession, _, _) => None
      case (_, clientUrn, None)           => Some(Anonymous(ClientApplication(clientUrn)))
      case (_, clientUrn, Some(user))     => Some(LoggedIn(ClientApplication(clientUrn), user))
    }
  }
}
