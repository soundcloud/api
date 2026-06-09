package com.soundcloud.apipublic.client.applications

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError, Outcome}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class ClientApplicationMetadataClient(clientApplicationMetadata: JsonClient) {
  def postApplication(
      user: Urn,
      credentialUrn: Urn,
      name: String,
      description: String,
      website: Option[String]
  ): Future[Outcome[Urn]] = {
    clientApplicationMetadata
      .post(
        Path("/application"),
        Params.empty,
        Headers.empty(),
        Some(
          Json.stringify(
            Json.obj(
              "name" -> name,
              "description" -> description,
              "url" -> website,
              "owner_urn" -> user.toString,
              "default_credential_urn" -> credentialUrn.toString
            )
          )
        )
      )
      .map { response =>
        response.status match {
          case Status.Successful(_) =>
            scala.util.Try(Json.parse(response.contentString)).flatMap { json =>
              scala.util.Try((json \ "urn").as[Urn])
            } match {
              case scala.util.Success(urn) => Good(urn)
              case scala.util.Failure(_) =>
                Bad(HttpServiceError(HttpResponseFields(Status.InternalServerError.code)))
            }
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }
  }
}
