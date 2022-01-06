package com.soundcloud.apipublic.client.vk2

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class Vk2Client(httpClient: JsonClient) {

  def checkSignIn(
      applicationUrn: Urn,
      clientId: String,
      username: String,
      ip: String,
      userAgent: Option[String]
  ): Future[Vk2Response] = {
    val response = httpClient
      .post(
        Path() / "signin",
        body = Some(
          Json.stringify(
            Json
              .obj(
                "test" -> false,
                // You might be wondering why the system_urn here is public-api
                // rather than, e.g., api-public. Essentially, vk2 uses a
                // hardcoded list of system URNs as a way of verifying that requests
                // came from a "valid place" (e.g. that we don't get sign-up requests
                // that claim to be from api-web, which does not support sign-up).
                // By using the public-api URN to signal that a request came from
                // PAS, we avoid coupling the name or URN of this system to vk2 code.
                "system_urn" -> "soundcloud:systems:public-api",
                "application_urn" -> applicationUrn.toString,
                "client_id" -> clientId,
                "username" -> username,
                "ip" -> ip,
                "useragent" -> userAgent
              )
          )
        )
      )

    response.map { resp =>
      resp.status match {
        case Status.Ok => Human(readCorrelationId(resp.contentString))
        case Status.PreconditionRequired => SuspectedBot
        case Status.Forbidden => Bot
        case unknown => throw new Exception(s"unexpected response status from Vk2 $unknown")
      }
    }
  }

  private def readCorrelationId(responseBody: String) = {
    (Json.parse(responseBody) \ "correlation_id").as[String]
  }
}
