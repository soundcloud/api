package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.client.applications.{
  ClientApplication,
  ClientApplicationDetail,
  ClientApplicationMetadataClient,
  ClientApplicationsClient,
  CreatorSubscriptionsClient
}
import com.soundcloud.apipublic.handler.representation.ClientApplicationsPage
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome.{ApplicationError, Bad, Good, HttpServiceError, NotValid, Outcome}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.periskop.client.Severity
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsError, JsSuccess, Json, OWrites, Reads, Writes}

class ClientApplicationsHandler(
    userAuthentication: UserAuthentication,
    applicationsClient: ClientApplicationsClient,
    metadataClient: ClientApplicationMetadataClient,
    creatorSubscriptionsClient: CreatorSubscriptionsClient,
    exceptionCollector: ExceptionCollector
) {
  def getUserApplications(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      applicationsClient.gatherUserApplicationDetails(session).flatMap {
        case Good(apps) =>
          Future.value(JsonResponseBuilder.ok(Json.stringify(Json.toJson(ClientApplicationsPage(apps)))))
        case Bad(error) =>
          respondToBadOutcome("client-applications-gather-user-application-details-failure", error)
      }
    }
  }

  def createUserApplication(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      parseCreateApplicationRequest(request) match {
        case Good(appRequest) =>
          applicationsClient.gatherUserApplications(session).flatMap {
            case Good(existingApps) =>
              applicationsClient.gatherUserApplicationDetails(session).flatMap {
                case Good(existingAppDetails) =>
                  evaluateCreateFirstEligibility(appRequest, existingAppDetails) match {
                    case Some(rejection) =>
                      Future.value(createFirstForbiddenResponse(rejection))
                    case None =>
                      createApplicationWithSubscriptionCheck(session, appRequest, existingApps)
                  }
                case Bad(error) =>
                  respondToBadOutcome("client-applications-gather-user-application-details-failure", error)
              }
            case Bad(error) =>
              respondToBadOutcome("client-applications-gather-user-applications-failure", error)
          }
        case Bad(_) =>
          Future.value(Response(Status.BadRequest))
      }
    }
  }

  private def createApplicationWithSubscriptionCheck(
      session: UserSession,
      appRequest: CreateApplicationRequest,
      existingApps: Seq[ClientApplication]
  ): Future[Response] =
    creatorSubscriptionsClient.hasActiveProUnlimited(session, session.getUser).flatMap {
      case Good(true) =>
        applicationsClient.createCredential().flatMap {
          case Good(credential) =>
            (
              credential.urn.flatMap(Urn.parse(_).toOption),
              credential.client_id,
              credential.client_secret
            ) match {
              case (Some(credentialUrn), Some(clientId), Some(clientSecret)) =>
                finalizeApplicationCreation(
                  session,
                  appRequest,
                  existingApps,
                  credentialUrn,
                  clientId,
                  clientSecret
                )
              case _ =>
                logAndReturnInternalServerError(
                  "client-applications-create-first-invalid-or-missing-credentials",
                  s"urn=${credential.urn.getOrElse("None")}, has_client_id=${credential.client_id.isDefined}, has_client_secret=${credential.client_secret.isDefined}"
                )
            }
          case Bad(error) =>
            respondToBadOutcome(
              "client-applications-create-first-credential-failure",
              error
            )
        }
      case Good(false) =>
        Future.value(
          createFirstForbiddenResponse(CreateFirstRejection.ApplicationCreationNotAvailable)
        )
      case Bad(error) =>
        respondToBadOutcome(
          "client-applications-create-first-subscription-check-failure",
          error
        )
    }

  private def finalizeApplicationCreation(
      session: UserSession,
      appRequest: CreateApplicationRequest,
      existingApps: Seq[ClientApplication],
      credentialUrn: Urn,
      clientId: String,
      clientSecret: String
  ): Future[Response] = {
    val createdResponse = Future.value(
      JsonResponseBuilder(
        status = Status.Created,
        body = Json.stringify(
          Json.obj(
            "client_id" -> clientId,
            "client_secret" -> clientSecret
          )
        )
      ).build
    )

    existingApps.headOption match {
      case Some(existingApp) =>
        applicationsClient.finishCredential(existingApp.urn, credentialUrn).flatMap {
          case Good(_) => createdResponse
          case Bad(error) =>
            respondToBadOutcome("client-applications-create-first-finalize-failure", error)
        }
      case None =>
        metadataClient
          .postApplication(
            user = session.getUser,
            credentialUrn = credentialUrn,
            name = appRequest.name,
            description = appRequest.description,
            website = appRequest.website
          )
          .flatMap {
            case Good(applicationUrn) =>
              applicationsClient.finishCredential(applicationUrn, credentialUrn).flatMap {
                case Good(_) => createdResponse
                case Bad(error) =>
                  respondToBadOutcome("client-applications-create-first-finalize-failure", error)
              }
            case Bad(error) =>
              respondToBadOutcome(
                "client-applications-create-first-metadata-failure",
                error
              )
          }
    }
  }

  private def evaluateCreateFirstEligibility(
      app: CreateApplicationRequest,
      existingAppDetails: Seq[ClientApplicationDetail]
  ): Option[CreateFirstRejection] =
    if (existingAppDetails.nonEmpty) {
      Some(CreateFirstRejection.UserAlreadyHasApplication)
    } else if (CreateFirstRejection.nameContainsSoundCloud(app.name)) {
      Some(CreateFirstRejection.ApplicationNameNotAllowed)
    } else if (CreateFirstRejection.websiteContainsSoundCloud(app.website)) {
      Some(CreateFirstRejection.ApplicationWebsiteNotAllowed)
    } else {
      None
    }

  private def createFirstForbiddenResponse(rejection: CreateFirstRejection): Response =
    JsonResponseBuilder.forbidden(
      Json.stringify(
        Json.toJson(
          CreateFirstApplicationErrorResponse(
            Seq(CreateFirstApplicationError(rejection.code, rejection.errorMessage))
          )
        )
      )
    )

  private def respondToBadOutcome(context: String, error: ApplicationError): Future[Response] =
    error match {
      case HttpServiceError(fields) if fields.statusCode >= 400 && fields.statusCode < 500 =>
        Future.value(Response(Status.fromCode(fields.statusCode)))
      case _ =>
        logAndReturnInternalServerError(context, Bad(error))
    }

  private def logAndReturnInternalServerError(context: String, details: Any): Future[Response] = {
    exceptionCollector.addMessage(
      context,
      details.toString,
      Severity.Warning,
      true
    )
    Future.value(Response(Status.InternalServerError))
  }

  private def parseCreateApplicationRequest(request: Request): Outcome[CreateApplicationRequest] =
    scala.util.Try(Json.parse(request.contentString)).toOption match {
      case Some(json) =>
        json.validate[CreateApplicationRequest] match {
          case JsSuccess(value, _) => Good(value)
          case _: JsError => NotValid("").bad
        }
      case None => NotValid("").bad
    }
}

sealed abstract class CreateFirstRejection(val code: String, val errorMessage: String)

object CreateFirstRejection {
  case object UserAlreadyHasApplication
      extends CreateFirstRejection(
        "user_already_has_application",
        "You already have a registered application."
      )

  case object ApplicationNameNotAllowed
      extends CreateFirstRejection(
        "application_name_not_allowed",
        "This application name is not allowed."
      )

  case object ApplicationWebsiteNotAllowed
      extends CreateFirstRejection(
        "application_website_not_allowed",
        "This website URL is not allowed."
      )

  case object ApplicationCreationNotAvailable
      extends CreateFirstRejection(
        "application_creation_not_available",
        "Application registration is not available for your account."
      )

  private val soundCloudNamePattern = "(?i)sound[\\s_.-]*cloud".r

  def nameContainsSoundCloud(name: String): Boolean =
    soundCloudNamePattern.findFirstIn(name).isDefined

  def websiteContainsSoundCloud(website: Option[String]): Boolean =
    website.exists { url =>
      val lower = url.toLowerCase
      val host = scala.util
        .Try {
          val uriString = if (lower.contains("://")) lower else s"http://$lower"
          val uri = new java.net.URI(uriString)
          Option(uri.getHost).orElse(Option(uri.getAuthority)).getOrElse("")
        }
        .getOrElse("")
      host.nonEmpty && soundCloudNamePattern.findFirstIn(host).isDefined
    }
}

case class CreateFirstApplicationError(code: String, error_message: String)

object CreateFirstApplicationError {
  implicit val writesCreateFirstApplicationError: Writes[CreateFirstApplicationError] =
    Json.writes[CreateFirstApplicationError]
}

case class CreateFirstApplicationErrorResponse(errors: Seq[CreateFirstApplicationError])

object CreateFirstApplicationErrorResponse {
  implicit val writesCreateFirstApplicationErrorResponse: Writes[CreateFirstApplicationErrorResponse] =
    Json.writes[CreateFirstApplicationErrorResponse]
}

case class CreateApplicationRequest(name: String, description: String, website: Option[String])
object CreateApplicationRequest {
  implicit val readsCreateApplicationRequest: Reads[CreateApplicationRequest] = Json.reads[CreateApplicationRequest]
  implicit val writesCreateApplicationRequest: OWrites[CreateApplicationRequest] = Json.writes[CreateApplicationRequest]
}
