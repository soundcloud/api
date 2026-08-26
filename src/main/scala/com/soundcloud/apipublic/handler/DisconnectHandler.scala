package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.client.TokenDispenserClient
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.periskop.client.Severity
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class DisconnectHandler(
    userAuthentication: UserAuthentication,
    tokenDispenserClient: TokenDispenserClient,
    exceptionCollector: ExceptionCollector
) {
  def disconnect(request: HandlerRequest): Future[Response] =
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      sessionAgentApplication(session.getAgent) match {
        case Some(applicationUrn) =>
          tokenDispenserClient.invalidateTokensForApplication(session, applicationUrn).value.map {
            case Good(_) =>
              Response(Status.NoContent)
            case Bad(error) =>
              logAndReturnInternalServerError("disconnect-failure", error)
          }
        case None =>
          Future.value(JsonResponseBuilder.badRequest())
      }
    }

  private def sessionAgentApplication(agent: Urn): Option[Urn] =
    Option(agent).filter(_.collection == "applications")

  private def logAndReturnInternalServerError(context: String, details: Any): Response = {
    val message = details match {
      case UnexpectedError(t: Throwable) => formatThrowable(t)
      case t: Throwable => formatThrowable(t)
      case other => other.toString
    }
    exceptionCollector.addMessage(
      context,
      message,
      Severity.Warning,
      true
    )
    Response(Status.InternalServerError)
  }

  private def formatThrowable(t: Throwable): String =
    s"${t.toString}\n${t.getStackTrace.take(10).mkString("\n")}"
}
