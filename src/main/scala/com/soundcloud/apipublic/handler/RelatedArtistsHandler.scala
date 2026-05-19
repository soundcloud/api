package com.soundcloud.apipublic.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.service.{RelatedArtistsPagination, RelatedArtistsService}
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}

class RelatedArtistsHandler(
    userAuthentication: UserAuthentication,
    relatedArtistsService: RelatedArtistsService,
    baseUrl: String
) {

  def handleRelatedArtists(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { (session: UserSession) =>
      val pagination =
        RelatedArtistsPagination.fromRequest(request.params, new URL(new URL(baseUrl), request.uri))
      val hasLinkedPartitioning = request.params.contains("linked_partitioning")

      Try(getUserUrn(request.routeParams("id"))) match {
        case Return(urn) =>
          val collection = performGetRelatedArtists(session, urn, pagination)
          CollectionResponse.handleCollectionResponse(collection, hasLinkedPartitioning)
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def performGetRelatedArtists(
      session: UserSession,
      userUrn: Urn,
      pagination: RelatedArtistsPagination
  ): Future[Outcome[Collection[UserRepresentation]]] = {
    relatedArtistsService
      .relatedArtists(session, userUrn, pagination)
      .map {
        case Some(res) => Good(res)
        case None => NotFound().bad
      }
  }
}
