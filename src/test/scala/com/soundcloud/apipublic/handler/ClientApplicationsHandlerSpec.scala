package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.applications.{
  ClientApplication,
  ClientApplicationDetail,
  ClientApplicationMetadataClient,
  ClientApplicationsClient,
  ClientCredential,
  CreatorSubscriptionsClient
}
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.util.Future
import org.mockito.Mockito.{never, verify}
import play.api.libs.json.{JsNull, Json}

class ClientApplicationsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val userUrn = Urn("soundcloud", "users", "2")
    val appUrn = Urn("soundcloud", "applications", "123")
    val credentialUrn = Urn("soundcloud", "credentials", "456")
    val session = new UserSessionBuilder()
      .setUser(userUrn)
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .build()

    val applicationsClient = mock[ClientApplicationsClient]
    val metadataClient = mock[ClientApplicationMetadataClient]
    val creatorSubscriptionsClient = mock[CreatorSubscriptionsClient]
    val exceptionCollector = mock[ExceptionCollector]

    val handler = new ClientApplicationsHandler(
      new FakeUserAuthentication(session),
      applicationsClient,
      metadataClient,
      creatorSubscriptionsClient,
      exceptionCollector
    )

    override def routingDefinitions(): List[(Method, String, com.soundcloud.jvmkit.module.http.server.Handler)] =
      Routing.forClientApplicationsHandler(handler)

    def listApps(): Response = get("/me/apps")

    def createApp(body: String): Response = post("/me/apps", body = body)

    def createAppErrorCode(response: Response): String =
      ((Json.parse(response.contentString) \ "errors")(0) \ "code").as[String]

    def createAppErrorMessage(response: Response): String =
      ((Json.parse(response.contentString) \ "errors")(0) \ "error_message").as[String]
  }

  "CreateFirstRejection.websiteContainsSoundCloud" >> {
    "does not throw when the website URL has no host" in {
      CreateFirstRejection.websiteContainsSoundCloud(Some("https://")) must beFalse
    }
  }

  "GET /me/apps" >> {
    "returns 401 when user is anonymous" in new Context {
      override val session: UserSession = anonymousSession
      listApps().status ==== Status.Unauthorized
    }

    "returns 200 with apps and credentials" in new Context {
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(
        Good(
          Seq(
            ClientApplicationDetail(
              urn = Some(appUrn),
              owner = Some(userUrn),
              name = "Some Application",
              description = Some("App Description"),
              url = None,
              credentials = Some(credentialUrn.toString),
              redirect_uri = Some("https://soundcloud.com/callback"),
              client_id = Some("1234567890"),
              client_secret = Some("ABCDEFG")
            )
          )
        )
      )

      val response = listApps()
      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== Json.obj(
        "next_href" -> JsNull,
        "query_urn" -> JsNull,
        "collection" -> Json.arr(
          Json.obj(
            "urn" -> "soundcloud:applications:123",
            "owner" -> "soundcloud:users:2",
            "name" -> "Some Application",
            "description" -> "App Description",
            "url" -> JsNull,
            "redirect_uri" -> "https://soundcloud.com/callback",
            "client_id" -> "1234567890",
            "client_secret" -> "ABCDEFG",
            "credentials" -> "soundcloud:credentials:456"
          )
        )
      )
    }

    "returns 200 with empty collection when user has no apps" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response = listApps()
      response.status ==== Status.Ok
      response.contentString === """{"collection":[],"next_href":null,"query_urn":null}"""
    }

    "returns 500 when applications lookup fails" in new Context {
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(
        Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
      )

      listApps().status ==== Status.InternalServerError
    }
  }

  "POST /me/apps" >> {
    "returns 401 when user is anonymous" in new Context {
      override val session: UserSession = anonymousSession
      createApp("""{"name":"my app","description":"desc"}""").status ==== Status.Unauthorized
    }

    "returns 400 when payload is invalid" in new Context {
      createApp(""" """).status ==== Status.BadRequest
    }

    "returns 403 and user_already_has_application when user already has an app with active credentials" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(
        Good(
          Seq(
            ClientApplication(
              urn = appUrn,
              owner = userUrn,
              name = "existing",
              description = Some("desc"),
              url = None
            )
          )
        )
      )
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(
        Good(
          Seq(
            ClientApplicationDetail(
              urn = Some(appUrn),
              owner = Some(userUrn),
              name = "existing",
              description = Some("desc"),
              url = None,
              credentials = Some(credentialUrn.toString),
              redirect_uri = Some("https://soundcloud.com/callback"),
              client_id = Some("1234567890"),
              client_secret = Some("ABCDEFG")
            )
          )
        )
      )

      val response = createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.UserAlreadyHasApplication.code
      createAppErrorMessage(response) ==== CreateFirstRejection.UserAlreadyHasApplication.errorMessage
    }

    "returns 500 when gathering user applications fails" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(
        Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
      )

      createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""").status ==== Status.InternalServerError
    }

    "returns 500 when gathering user application details fails" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(
        Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
      )

      createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""").status ==== Status.InternalServerError
    }

    "allows creation when user has application metadata but no active credentials" in new Context {
      val clientId = "abc-client-id"
      val clientSecret = "xyz-client-secret"
      applicationsClient.gatherUserApplications(session) returns Future.value(
        Good(
          Seq(
            ClientApplication(
              urn = appUrn,
              owner = userUrn,
              name = "existing",
              description = Some("desc"),
              url = None
            )
          )
        )
      )
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some(clientId),
            client_secret = Some(clientSecret),
            revoked_at = None
          )
        )
      )
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(Good(()))

      val response = createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== Json.obj(
        "client_id" -> clientId,
        "client_secret" -> clientSecret
      )
      verify(metadataClient, never()).postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://example.com")
      )
    }

    "returns 403 and application_name_not_allowed when app name references SoundCloud" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response =
        createApp("""{"name":"Sound Cloud Tools","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationNameNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationNameNotAllowed.errorMessage
    }

    "returns 403 and application_website_not_allowed when website references SoundCloud subdomain" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response = createApp(
        """{"name":"my app","description":"desc","website":"https://www.myapp.soundcloud.com"}"""
      )
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.errorMessage
    }

    "returns 403 and application_website_not_allowed when website is soundcloud.com" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response =
        createApp("""{"name":"my app","description":"desc","website":"https://soundcloud.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.errorMessage
    }

    "returns 403 and application_website_not_allowed when website uses soundcloud TLD variant" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response =
        createApp("""{"name":"my app","description":"desc","website":"https://soundcloud.net"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.errorMessage
    }

    "returns 403 and application_website_not_allowed when website host impersonates soundcloud" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response =
        createApp("""{"name":"my app","description":"desc","website":"https://soundcloud.net.attacker.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.errorMessage
    }

    "returns 403 and application_website_not_allowed when website host uses underscore" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))

      val response =
        createApp("""{"name":"my app","description":"desc","website":"https://soundcloud_support.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationWebsiteNotAllowed.errorMessage
    }

    "allows website when soundcloud appears only in the path" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some("1234567890"),
            client_secret = Some("ABCDEFG"),
            revoked_at = None
          )
        )
      )
      metadataClient.postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://github.com/soundcloud/repo")
      ) returns Future.value(Good(appUrn))
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(Good(()))

      createApp("""{"name":"my app","description":"desc","website":"https://github.com/soundcloud/repo"}""").status ====
        Status.Created
    }

    "allows website when host contains underscore and soundcloud appears only in the path" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some("1234567890"),
            client_secret = Some("ABCDEFG"),
            revoked_at = None
          )
        )
      )
      metadataClient.postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://my_app.com/soundcloud-callback")
      ) returns Future.value(Good(appUrn))
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(Good(()))

      createApp("""{"name":"my app","description":"desc","website":"https://my_app.com/soundcloud-callback"}""").status ====
        Status.Created
    }

    "returns 403 and application_creation_not_available when user has no active Artist Pro subscription" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(false))

      val response = createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Forbidden
      createAppErrorCode(response) ==== CreateFirstRejection.ApplicationCreationNotAvailable.code
      createAppErrorMessage(response) ==== CreateFirstRejection.ApplicationCreationNotAvailable.errorMessage
    }

    "returns 500 when subscription lookup fails" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(
        Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
      )

      createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""").status ==== Status.InternalServerError
    }

    "returns 500 without posting metadata when credentials are incomplete" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some("abc-client-id"),
            client_secret = None,
            revoked_at = None
          )
        )
      )

      createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""").status ==== Status.InternalServerError
      verify(metadataClient, never()).postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://example.com")
      )
    }

    "returns 500 when finalize fails" in new Context {
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some("abc-client-id"),
            client_secret = Some("xyz-client-secret"),
            revoked_at = None
          )
        )
      )
      metadataClient.postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://example.com")
      ) returns Future.value(Good(appUrn))
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(
        Bad(HttpServiceError(HttpResponseFields(Status.BadGateway.code)))
      )

      createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""").status ==== Status.InternalServerError
    }

    "returns 201 when creating the first application succeeds" in new Context {
      val clientId = "abc-client-id"
      val clientSecret = "xyz-client-secret"
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some(clientId),
            client_secret = Some(clientSecret),
            revoked_at = None
          )
        )
      )
      metadataClient.postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://example.com")
      ) returns Future.value(Good(appUrn))
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(Good(()))

      val response = createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== Json.obj(
        "client_id" -> clientId,
        "client_secret" -> clientSecret
      )
    }

    "returns 201 with credentials when finalize returns 204 No Content" in new Context {
      val clientId = "abc-client-id"
      val clientSecret = "xyz-client-secret"
      applicationsClient.gatherUserApplications(session) returns Future.value(Good(Seq.empty))
      applicationsClient.gatherUserApplicationDetails(session) returns Future.value(Good(Seq.empty))
      creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn) returns Future.value(Good(true))
      applicationsClient.createCredential() returns Future.value(
        Good(
          ClientCredential(
            urn = Some(credentialUrn.toString),
            redirect_uri = None,
            client_id = Some(clientId),
            client_secret = Some(clientSecret),
            revoked_at = None
          )
        )
      )
      metadataClient.postApplication(
        user = userUrn,
        credentialUrn = credentialUrn,
        name = "my app",
        description = "desc",
        website = Some("https://example.com")
      ) returns Future.value(Good(appUrn))
      applicationsClient.finishCredential(appUrn, credentialUrn) returns Future.value(Good(()))

      val response = createApp("""{"name":"my app","description":"desc","website":"https://example.com"}""")
      response.status ==== Status.Created
      Json.parse(response.contentString) ==== Json.obj(
        "client_id" -> clientId,
        "client_secret" -> clientSecret
      )
    }
  }
}
