package com.soundcloud.apipublic.client.applications

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Await
import org.mockito.Mockito.when
import play.api.libs.json.Json

class ClientApplicationMetadataClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val client = new ClientApplicationMetadataClient(jsonClient)
    val userUrn = Urn("soundcloud", "users", "123")
    val credentialUrn = Urn("soundcloud", "credentials", "456")
    val appUrn = Urn("soundcloud", "applications", "2")

    def okResponse(body: String): Response = {
      val response = Response(Status.Ok)
      response.setContentString(body)
      response
    }
  }

  "postApplication" >> {
    "returns the created application urn" in new Context {
      when(
        jsonClient.post(
          Path("/application"),
          Params.empty,
          Headers.empty(),
          Some(
            Json.stringify(
              Json.obj(
                "name" -> "My App",
                "description" -> "A test app",
                "url" -> "https://example.com",
                "owner_urn" -> userUrn.toString,
                "default_credential_urn" -> credentialUrn.toString
              )
            )
          )
        )
      ).thenReturn(
        com.twitter.util.Future.value(
          okResponse(Json.stringify(Json.obj("urn" -> appUrn.toString)))
        )
      )

      Await.result(
        client.postApplication(
          user = userUrn,
          credentialUrn = credentialUrn,
          name = "My App",
          description = "A test app",
          website = Some("https://example.com")
        )
      ) ==== Good(appUrn)
    }

    "returns Bad when application creation fails" in new Context {
      when(
        jsonClient.post(
          Path("/application"),
          Params.empty,
          Headers.empty(),
          Some(
            Json.stringify(
              Json.obj(
                "name" -> "My App",
                "description" -> "A test app",
                "url" -> None,
                "owner_urn" -> userUrn.toString,
                "default_credential_urn" -> credentialUrn.toString
              )
            )
          )
        )
      ).thenReturn(com.twitter.util.Future.value(Response(Status.BadRequest)))

      Await.result(
        client.postApplication(
          user = userUrn,
          credentialUrn = credentialUrn,
          name = "My App",
          description = "A test app",
          website = None
        )
      ) must beAnInstanceOf[Bad]
    }
  }
}
