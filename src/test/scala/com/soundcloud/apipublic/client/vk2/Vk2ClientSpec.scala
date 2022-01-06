package com.soundcloud.apipublic.client.vk2

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.{JsValue, Json}

class Vk2ClientSpec extends Specification with Mockito {

  def matchesJson(expected: JsValue): Matcher[String] = beEqualTo(expected) ^^ (Json.parse(_: String))

  val emptyHeaders: Matcher[Headers] = beNull or beTheSameAs(Headers.empty)
  val emptyParams: Matcher[Params] = beNull or beTheSameAs(Params.empty)

  trait Context extends Scope {
    val mockJsonClient = mock[JsonClient]

    val applicationUrn = Urn("soundcloud", "applications", "app_id")
    val clientId = "clientId"
    val username = "username"
    val ip = "10.0.0.1"
    val userAgent = Some("some-user-agent")

    val correlationId = "some_correlation_id"
    val humanResponse = JsonResponseBuilder.ok(body = Json.stringify(Json.obj("correlation_id" -> correlationId)))

    val subject = new Vk2Client(mockJsonClient)

    def result() = Await.result(subject.checkSignIn(applicationUrn, clientId, username, ip, userAgent))
  }

  "vk2 should be called with the correct parameter" in new Context {
    val expectedBody = Json
      .obj(
        "test" -> false,
        "system_urn" -> "soundcloud:systems:public-api",
        "application_urn" -> applicationUrn.toString,
        "client_id" -> clientId,
        "username" -> username,
        "ip" -> ip,
        "useragent" -> userAgent
      )

    mockJsonClient.post(any, any, any, any) returns Future.value(humanResponse)

    result()

    there was one(mockJsonClient).post(
      beEqualTo(Path() / "signin"): Matcher[Path],
      emptyParams,
      emptyHeaders,
      beSome(matchesJson(expectedBody))
    )
  }

  "when Vk2 responds with Ok, returns Human" in new Context {
    mockJsonClient.post(any, any, any, any) returns Future.value(humanResponse)

    result ==== Human(correlationId)
  }

  "when Vk2 responds with PreconditionRequired, returns SuspectedBot" in new Context {
    mockJsonClient.post(any, any, any, any) returns
      Future.value(JsonResponseBuilder(status = Status.PreconditionRequired).build)

    result ==== SuspectedBot
  }

  "when Vk2 responds with Forbidden, returns Bot" in new Context {
    mockJsonClient.post(any, any, any, any) returns Future.value(JsonResponseBuilder.forbidden())

    result ==== Bot
  }
}
