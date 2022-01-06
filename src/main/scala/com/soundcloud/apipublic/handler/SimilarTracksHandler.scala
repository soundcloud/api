package com.soundcloud.apipublic.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.handler.representation.collection.CollectionResponse
import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.SimilarTracksService
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackPagination, TrackRepresentation}
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
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
      val access = AccessParamsExtractor.unapply(request.params)

      Try(getTrackUrn(request)) match {
        case Return(urn) =>
          val similarTracksCollection = performGetSimilarTracks(session, urn, access, pagination)
          CollectionResponse.handleCollectionResponse(similarTracksCollection, hasLinkedPartitioning)
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
      }
    }
  }

  def performGetSimilarTracks(
      session: UserSession,
      trackUrn: Urn,
      access: AccessParams,
      pagination: TrackPagination
  ): Future[Outcome[Collection[TrackRepresentation]]] = {
    similarTracksService
      .similarTracks(session, trackUrn, access, pagination)
      .map {
        case Some(res) => Good(res)
        case None => NotFound().bad
      }
  }
}
