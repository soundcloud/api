package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.SystemPlaylistsClient
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.UserRepresentationsService
import com.twitter.util.Future

class RelatedArtistsService(
    userRepresentationsService: UserRepresentationsService,
    systemPlaylistsClient: SystemPlaylistsClient
) {

  def relatedArtists(
      session: UserSession,
      userUrn: Urn,
      pagination: RelatedArtistsPagination
  ): Future[Option[Collection[UserRepresentation]]] = {
    val backendPageSize = math.min(200, math.max(pagination.offset + pagination.limit, 50))
    for {
      maybeCreators <- systemPlaylistsClient.fetchSimilarCreators(session, userUrn, backendPageSize)
      result <- maybeCreators match {
        case None => Future.value(None)
        case Some(creators) =>
          val orderedUrns = creators.users
          val pageUrns = pagination.sliceOrderedUrns(orderedUrns)
          userRepresentationsService
            .users(session, pageUrns, fetchSubscriptions = false)
            .map { users =>
              Some(Collection(users, pagination.nextHref(orderedUrns.size)))
            }
      }
    } yield result
  }
}
