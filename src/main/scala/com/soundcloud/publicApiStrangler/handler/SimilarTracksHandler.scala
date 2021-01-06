package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.handler.representation.collection.CollectionResponse
import com.soundcloud.publicApiStrangler.service.SimilarTracksService
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TrackRepresentation}
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Return, Throw, Try}

/**
  * Overrides the public api endpoint used to retrieve similar tracks.
  */
class SimilarTracksHandler(
    userAuthentication: UserAuthentication,
    similarTracksService: SimilarTracksService,
    baseUrl: String
) {

  def handleSimilarTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { (session: UserSession) =>
      val pagination = TrackPagination.fromRequest(request.params, new URL(baseUrl + request.uri))
      val hasLinkedPartitioning = request.params.contains("linked_partitioning")

      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          val similarTracksCollection = performGetSimilarTracks(session, urn, pagination)
          CollectionResponse.handleCollectionResponse(similarTracksCollection, hasLinkedPartitioning)
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def performGetSimilarTracks(
      session: UserSession,
      trackUrn: Urn,
      pagination: TrackPagination
  ): Future[Outcome[Collection[TrackRepresentation]]] = {
    similarTracksService
      .similarTracks(session, trackUrn, pagination)
      .map {
        case Some(res) => Good(res)
        case None => NotFound().bad
      }
  }
}
