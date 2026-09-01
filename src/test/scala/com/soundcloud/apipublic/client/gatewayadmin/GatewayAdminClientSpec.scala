package com.soundcloud.apipublic.client.gatewayadmin

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Await
import org.mockito.Mockito.when
import play.api.libs.json.Json

class GatewayAdminClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val client = new GatewayAdminClient(jsonClient)
    val session = new UserSessionBuilder().setUser(Urn("soundcloud", "users", "123")).build()
    val appUrn = Urn("soundcloud", "applications", "2")

    def okResponse(body: String): Response = {
      val response = Response(Status.Ok)
      response.setContentString(body)
      response
    }
  }

  "getApplication" >> {
    "returns metadata with parsed access_label" in new Context {
      when(
        jsonClient.getWithSession(
          session,
          Path(s"/applications/${appUrn.toString}"),
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(
        com.twitter.util.Future.value(
          okResponse(
            Json.stringify(
              Json.obj(
                "key" -> "abc123",
                "key_hash" -> "hash",
                "is_partner" -> false,
                "access_label" -> "low"
              )
            )
          )
        )
      )

      Await.result(client.getApplication(session, appUrn)) ==== Good(
        GatewayAdminApplication(Some("low"))
      )
    }

    "returns metadata without access label when the app is not in gateway admin" in new Context {
      when(
        jsonClient.getWithSession(
          session,
          Path(s"/applications/${appUrn.toString}"),
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(Response(Status.NotFound)))

      Await.result(client.getApplication(session, appUrn)) ==== Good(
        GatewayAdminApplication(None)
      )
    }

    "returns Bad when gateway admin lookup fails" in new Context {
      when(
        jsonClient.getWithSession(
          session,
          Path(s"/applications/${appUrn.toString}"),
          Params.empty,
          Headers.empty()
        )
      ).thenReturn(com.twitter.util.Future.value(Response(Status.BadGateway)))

      Await.result(client.getApplication(session, appUrn)) must beAnInstanceOf[Bad]
    }
  }
}
