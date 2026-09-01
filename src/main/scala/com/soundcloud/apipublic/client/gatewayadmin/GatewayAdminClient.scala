package com.soundcloud.apipublic.client.gatewayadmin

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError, Outcome}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class GatewayAdminClient(gatewayAdmin: JsonClient) {
  def getApplication(session: UserSession, application: Urn): Future[Outcome[GatewayAdminApplication]] =
    gatewayAdmin
      .getWithSession(
        session,
        Path(s"/applications/${application.toString}"),
        Params.empty,
        Headers.empty()
      )
      .map { response =>
        response.status match {
          case Status.Successful(_) =>
            scala.util.Try(GatewayAdminApplicationMapper.fromJson(Json.parse(response.contentString))) match {
              case scala.util.Success(metadata) => Good(metadata)
              case scala.util.Failure(_) =>
                Bad(HttpServiceError(HttpResponseFields(Status.InternalServerError.code)))
            }
          case Status.NotFound =>
            Good(GatewayAdminApplication(None))
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }
}
