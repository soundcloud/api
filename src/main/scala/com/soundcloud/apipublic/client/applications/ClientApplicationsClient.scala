package com.soundcloud.apipublic.client.applications

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError, Outcome}
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.slf4j.Logger
import play.api.libs.json.{JsValue, Json}

private case class Application(urn: Urn)
private case class ChangeRedirectRequest(application: Application, redirect_uri: String, official: Boolean = false)

private object Application {
  implicit val readsClientApplication = Json.reads[Application]
  implicit val writesClientApplication = Json.writes[Application]
}

private object ChangeRedirectRequest {
  implicit val readsChangeRedirectRequest = Json.reads[ChangeRedirectRequest]
  implicit val writesChangeRedirectRequest = Json.writes[ChangeRedirectRequest]
}

case class ClientApplication(
    urn: Urn,
    owner: Urn,
    name: String,
    description: Option[String],
    url: Option[String]
)

case class ClientCredential(
    urn: Option[String],
    redirect_uri: Option[String],
    client_id: Option[String],
    client_secret: Option[String],
    revoked_at: Option[String]
)

object ClientApplicationsMapper {
  def fromList(json: JsValue): Seq[ClientApplication] = {
    (json \ "collection" \ "items").as[Seq[JsValue]].map { app =>
      ClientApplication(
        urn = (app \ "self" \ "urn").as[Urn],
        owner = (app \ "owner" \ "urn").as[Urn],
        name = (app \ "name").as[String],
        description = (app \ "description").asOpt[String],
        url = (app \ "url").asOpt[String]
      )
    }
  }
}

object ClientCredentialsMapper {

  /** List responses (e.g. GET with collection wrapper). */
  def fromList(json: JsValue): Seq[ClientCredential] = {
    (json \ "collection" \ "items")
      .asOpt[Seq[JsValue]]
      .getOrElse(Seq.empty)
      .map(parseOne)
  }

  /**
    * POST /credentials returns a single credential object at the root, not
    * `collection.items` — see clientapplications service response shape.
    */
  def fromCreateOrListResponse(json: JsValue): Seq[ClientCredential] = {
    (json \ "collection" \ "items").asOpt[Seq[JsValue]] match {
      case Some(items) => items.map(parseOne)
      case None if (json \ "self" \ "urn").asOpt[String].isDefined || (json \ "client_id").asOpt[String].isDefined =>
        Seq(parseOne(json))
      case None =>
        Seq.empty
    }
  }

  private def parseOne(credential: JsValue): ClientCredential =
    ClientCredential(
      urn = (credential \ "self" \ "urn").asOpt[String],
      redirect_uri = (credential \ "redirect_uri").asOpt[String],
      client_id = (credential \ "client_id").asOpt[String],
      client_secret = (credential \ "client_secret").asOpt[String],
      revoked_at = (credential \ "revoked_at").asOpt[String]
    )
}

class ClientApplicationsClient(clientApplications: JsonClient) {
  private lazy val logger: Logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val parseError =
    Bad(HttpServiceError(HttpResponseFields(Status.InternalServerError.code)))

  private def tryGood[T](thunk: => T): Outcome[T] =
    scala.util.Try(thunk) match {
      case scala.util.Success(value) => Good(value)
      case scala.util.Failure(exception) =>
        logger.warn("Failed to parse client applications response", exception)
        parseError
    }

  def gatherUserApplicationDetails(session: UserSession): Future[Outcome[Seq[ClientApplicationDetail]]] =
    gatherUserApplications(session).flatMap {
      case Good(apps) =>
        Future.collect(apps.map(enrichAppWithCredentials(_, session))).map { outcomes =>
          val errors = outcomes.collect { case Bad(err) => err }
          if (errors.nonEmpty) {
            Bad(errors.head)
          } else {
            Good(outcomes.collect { case Good(details) => details }.flatten)
          }
        }
      case Bad(error) =>
        Future.value(Bad(error))
    }

  def gatherUserApplications(session: UserSession): Future[Outcome[Seq[ClientApplication]]] =
    clientApplications
      .getWithSession(session, Path("/user/applications"), Params("user" -> session.getUser), Headers.empty())
      .map { response =>
        response.status match {
          case Status.Successful(_) =>
            tryGood(ClientApplicationsMapper.fromList(Json.parse(response.contentString)))
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }

  def enrichAppWithCredentials(
      app: ClientApplication,
      session: UserSession
  ): Future[Outcome[Seq[ClientApplicationDetail]]] =
    clientApplications
      .getWithSession(session, Path("/credentials"), Params("application" -> app.urn), Headers.empty())
      .map { response =>
        response.status match {
          case Status.Successful(_) =>
            scala.util.Try {
              ClientApplicationDetail.fromCredentialsExpansion(
                app,
                ClientCredentialsMapper.fromList(Json.parse(response.contentString))
              )
            } match {
              case scala.util.Success(details) => Good(details)
              case scala.util.Failure(exception) =>
                logger.warn(s"Failed to parse credentials response for application ${app.urn}", exception)
                parseError
            }
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }

  def createCredential(): Future[Outcome[ClientCredential]] = {
    clientApplications
      .post(
        Path("/credentials"),
        Params.empty,
        Headers.empty(),
        None
      )
      .map { response =>
        response.status match {
          case Status.Successful(_) =>
            scala.util.Try {
              ClientCredentialsMapper
                .fromCreateOrListResponse(Json.parse(response.contentString))
                .headOption
            } match {
              case scala.util.Success(Some(credential)) => Good(credential)
              case scala.util.Success(None) =>
                logger.warn("No credentials found in createCredential response")
                parseError
              case scala.util.Failure(exception) =>
                logger.warn("Failed to parse createCredential response", exception)
                parseError
            }
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }
  }

  def finishCredential(application: Urn, credential: Urn): Future[Outcome[Unit]] =
    clientApplications
      .put(
        Path("/credentials"),
        Params("credential" -> credential),
        Headers.empty(),
        Some(
          Json.stringify(
            Json.toJson(ChangeRedirectRequest(Application(application), ""))
          )
        )
      )
      .map { response =>
        response.status match {
          case Status.Successful(_) => Good(())
          case _ => Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }
}
