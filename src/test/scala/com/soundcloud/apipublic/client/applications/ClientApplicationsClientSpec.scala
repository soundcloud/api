package com.soundcloud.apipublic.client.applications

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Await
import org.mockito.Mockito.when
import play.api.libs.json.Json

class ClientApplicationsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val client = new ClientApplicationsClient(jsonClient)
    val userUrn = Urn("soundcloud", "users", "123")
    val appUrn = Urn("soundcloud", "applications", "2")
    val session = new UserSessionBuilder().setUser(userUrn).build()
    val applicationsPath = Path("/user/applications")
    val credentialsPath = Path("/credentials")
    val userParams = Params("user" -> userUrn)
    val applicationParams = Params("application" -> appUrn)

    def okResponse(body: String): Response = {
      val response = Response(Status.Ok)
      response.setContentString(body)
      response
    }
  }

  "gatherUserApplicationDetails" >> {
    "returns apps enriched with credentials" in new Context {
      when(jsonClient.getWithSession(session, applicationsPath, userParams, Headers.empty()))
        .thenReturn(
          com.twitter.util.Future.value(
            okResponse(Json.stringify(Fixtures.contentsOf("clientapplications", "app_collection")))
          )
        )
      when(jsonClient.getWithSession(session, credentialsPath, applicationParams, Headers.empty()))
        .thenReturn(
          com.twitter.util.Future.value(
            okResponse(Json.stringify(Fixtures.contentsOf("clientapplications", "client_credentials")))
          )
        )

      val result = Await.result(client.gatherUserApplicationDetails(session))
      result must beEqualTo(
        Good(
          Seq(
            ClientApplicationDetail(
              urn = Some(appUrn),
              owner = Some(userUrn),
              name = "listenAPP!",
              description = Some("Proof of concept, taking input from soundcloud / alonetone."),
              url = Some("http://listenApp.com"),
              redirect_uri = Some("https://soundcloud.com/callback"),
              client_id = Some("1234567890"),
              client_secret = Some("ABCDEFG"),
              credentials = Some("soundcloud:credentials:34")
            )
          )
        )
      )
    }

    "returns Bad when applications lookup fails" in new Context {
      when(jsonClient.getWithSession(session, applicationsPath, userParams, Headers.empty()))
        .thenReturn(com.twitter.util.Future.value(Response(Status.BadGateway)))

      Await.result(client.gatherUserApplicationDetails(session)) must beAnInstanceOf[Bad]
    }

    "returns Bad when applications response is invalid JSON" in new Context {
      when(jsonClient.getWithSession(session, applicationsPath, userParams, Headers.empty()))
        .thenReturn(com.twitter.util.Future.value(okResponse("not json")))

      Await.result(client.gatherUserApplicationDetails(session)) must beAnInstanceOf[Bad]
    }

    "returns Bad when credentials lookup fails" in new Context {
      when(jsonClient.getWithSession(session, applicationsPath, userParams, Headers.empty()))
        .thenReturn(
          com.twitter.util.Future.value(
            okResponse(Json.stringify(Fixtures.contentsOf("clientapplications", "app_collection")))
          )
        )
      when(jsonClient.getWithSession(session, credentialsPath, applicationParams, Headers.empty()))
        .thenReturn(com.twitter.util.Future.value(Response(Status.BadGateway)))

      Await.result(client.gatherUserApplicationDetails(session)) must beAnInstanceOf[Bad]
    }

    "returns Bad when credentials response is invalid JSON" in new Context {
      when(jsonClient.getWithSession(session, applicationsPath, userParams, Headers.empty()))
        .thenReturn(
          com.twitter.util.Future.value(
            okResponse(Json.stringify(Fixtures.contentsOf("clientapplications", "app_collection")))
          )
        )
      when(jsonClient.getWithSession(session, credentialsPath, applicationParams, Headers.empty()))
        .thenReturn(com.twitter.util.Future.value(okResponse("not json")))

      Await.result(client.gatherUserApplicationDetails(session)) must beAnInstanceOf[Bad]
    }
  }

  "createCredential" >> {
    "returns Bad when response body is invalid JSON" in new Context {
      when(jsonClient.post(Path("/credentials"), Params.empty, Headers.empty(), None))
        .thenReturn(com.twitter.util.Future.value(okResponse("not json")))

      Await.result(client.createCredential()) must beAnInstanceOf[Bad]
    }
  }
}
