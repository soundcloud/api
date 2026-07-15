package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.service.storefront.{StorefrontNotEligible, StorefrontService, StorefrontUpsert}
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{MediaType, Response}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.Json

class StorefrontHandler(
    userAuthentication: UserAuthentication,
    storefrontService: StorefrontService
) {

  def handleUpsertStorefront(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          request.mediaType match {
            case Some(MediaType.Json) => upsertFromJsonRequest(request, session, urn)
            case _ => Future.value(ErrorResponse.badRequest("Content-Type must be application/json"))
          }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  private def upsertFromJsonRequest(
      request: HandlerRequest,
      session: UserSession,
      trackUrn: Urn
  ): Future[Response] = {
    Try(Json.parse(request.contentString)) match {
      case Return(json) =>
        StorefrontUpsert.fromJson(json) match {
          case Right(upsert) =>
            storefrontService.upsertStorefront(session, trackUrn, upsert).map {
              case Good(storefront) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(storefront)))
              case Bad(NotFound(message)) => ErrorResponse.notFound(message)
              case Bad(NotAllowed(message)) => ErrorResponse.forbidden(message)
              case Bad(CustomError(StorefrontNotEligible, _)) =>
                ErrorResponse.forbidden(
                  "A creator subscription with external purchase options is required to manage a track storefront"
                )
              case Bad(NotValid(messages)) => ErrorResponse.badRequest(messages.mkString(", "))
              case _ => ResponseBuilder.internalServerError()
            }
          case Left(error) => Future.value(ErrorResponse.badRequest(error))
        }
      case Throw(_) => Future.value(ErrorResponse.badRequest("Request body must be valid JSON"))
    }
  }
}
