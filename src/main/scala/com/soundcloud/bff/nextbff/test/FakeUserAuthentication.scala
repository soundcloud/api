package com.soundcloud.bff.nextbff.test

import com.soundcloud.bff.finagle.{ResponseLike, Request => BffRequest}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn, UserSession}
import com.twitter.util.Future

class FakeUserAuthentication(session: UserSession) extends UserAuthentication {

  override def withUserSession[R: ResponseLike](request: BffRequest)(action: (UserSession) => Future[R]) = {
    action(session)
  }

  override def withLoggedInUser[R: ResponseLike](request: BffRequest)(action: (LoggedInUserSession, Urn) => Future[R]): Future[R] = {
    session match {
      case loggedInUser: LoggedInUserSession => action(loggedInUser, session.getUser)
      case _ => Future.value(ResponseLike[R].unauthorized)
    }
  }
}
