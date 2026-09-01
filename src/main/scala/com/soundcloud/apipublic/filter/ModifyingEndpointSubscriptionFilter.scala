package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.client.applications.CreatorSubscriptionsClient
import com.soundcloud.apipublic.client.gatewayadmin.{GatewayAdminApplication, GatewayAdminClient}
import com.soundcloud.apipublic.subscriptions.ModifyingEndpointSubscriptionEligibility
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.outcome.{Bad, Good}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ModifyingEndpointSubscriptionFilter(
    userAuthentication: UserAuthentication,
    gatewayAdminClient: GatewayAdminClient,
    creatorSubscriptionsClient: CreatorSubscriptionsClient,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {

  private val forbiddenMessage =
    "An active Pro Unlimited subscription is required for this action"

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    if (!ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(request.method, path)) {
      service(request)
    } else {
      userAuthentication.withLoggedInUser(HandlerRequest(request)) { (session, userUrn) =>
        Option(session.getAgent) match {
          case None => service(request)
          case Some(applicationUrn) =>
            gatewayAdminClient.getApplication(session, applicationUrn).flatMap {
              case Good(metadata) =>
                if (subscriptionCheckRequiredForApplication(metadata)) {
                  verifyProUnlimited(session, userUrn, service, request)
                } else {
                  service(request)
                }
              case Bad(_) => Future.value(Response(Status.InternalServerError))
            }
        }
      }
    }
  }

  private def subscriptionCheckRequiredForApplication(metadata: GatewayAdminApplication): Boolean =
    ModifyingEndpointSubscriptionEligibility.subscriptionCheckAppliesToAccessLabel(metadata.accessLabel)

  private def verifyProUnlimited(
      session: UserSession,
      userUrn: Urn,
      service: Service[Request, Response],
      request: Request
  ): Future[Response] =
    creatorSubscriptionsClient.hasActiveProUnlimited(session, userUrn).flatMap {
      case Good(true) => service(request)
      case Good(false) => Future.value(ErrorResponse.forbidden(forbiddenMessage))
      case Bad(_) => Future.value(Response(Status.InternalServerError))
    }
}
