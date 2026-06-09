package com.soundcloud.apipublic.client.applications

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good, HttpResponseFields, HttpServiceError, Outcome}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class CreatorSubscriptionsClient(submarineClient: JsonClient) {
  private val ActiveState = "active"
  private val ProUnlimitedPlan = "pro-unlimited"

  def hasActiveProUnlimited(session: UserSession, user: Urn): Future[Outcome[Boolean]] = {
    submarineClient
      .getWithSession(
        session,
        Path("/api") / "users" / user / "creator_subscriptions" / "active",
        Params.empty,
        Headers.empty()
      )
      .map { response =>
        response.status match {
          case Status.Ok =>
            scala.util.Try {
              val json = Json.parse(response.contentString)
              val state = (json \ "state").asOpt[String]
              val plan = (json \ "package" \ "plan").asOpt[String]
              state.contains(ActiveState) && plan.contains(ProUnlimitedPlan)
            } match {
              case scala.util.Success(hasSubscription) => Good(hasSubscription)
              case scala.util.Failure(_) =>
                Bad(HttpServiceError(HttpResponseFields(Status.InternalServerError.code)))
            }
          case Status.NotFound =>
            Good(false)
          case _ =>
            Bad(HttpServiceError(HttpResponseFields(response.statusCode)))
        }
      }
  }
}
