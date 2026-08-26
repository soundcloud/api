package com.soundcloud.apipublic.client

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.Await
import org.mockito.Mockito.when

class TokenDispenserClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val userUrn = Urn("soundcloud", "users", "123")
    val applicationUrn = Urn("soundcloud", "applications", "456")
    val session = new UserSessionBuilder().setUser(userUrn).build()

    val jsonClient = mock[JsonClient]
    val tokenDispenserClient = new TokenDispenserClient(jsonClient)
  }

  "invalidateTokensForApplication" >> {
    "returns UnexpectedError when postWithSession fails" in new Context {
      when(
        jsonClient.postWithSession(
          session,
          Path("/users") / userUrn / "invalidate-tokens" / applicationUrn,
          Params.empty,
          Headers.empty(),
          None
        )
      ).thenReturn(com.twitter.util.Future.exception(new RuntimeException("connection refused")))

      Await.result(tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn).value) match {
        case Bad(UnexpectedError(_)) => ok
        case other => ko(s"unexpected outcome $other")
      }
    }

    "returns UnexpectedError when client fails" in new Context {
      when(
        jsonClient.postWithSession(
          session,
          Path("/users") / userUrn / "invalidate-tokens" / applicationUrn,
          Params.empty,
          Headers.empty(),
          None
        )
      ).thenReturn(com.twitter.util.Future.value(ResponseBuilder.internalServerError("{}")))

      Await.result(tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn).value) match {
        case Bad(UnexpectedError(_)) => ok
        case other => ko(s"unexpected outcome $other")
      }
    }

    "returns success when client invalidates tokens for application" in new Context {
      when(
        jsonClient.postWithSession(
          session,
          Path("/users") / userUrn / "invalidate-tokens" / applicationUrn,
          Params.empty,
          Headers.empty(),
          None
        )
      ).thenReturn(com.twitter.util.Future.value(ResponseBuilder.ok()))

      Await.result(tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn).value) ==== Good(())
    }
  }
}
