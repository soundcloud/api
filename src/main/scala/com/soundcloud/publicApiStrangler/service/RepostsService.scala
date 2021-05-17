package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.Result
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserOrderingUtils.sortByProvidedUrns
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.twitter.util.Future

class RepostsService(userRepresentationService: UserRepresentationsService, repostsClient: RepostsClient) {

  def createRepost(session: UserSession, target: Urn, url: String): Future[Result] = {
    repostsClient.createRepost(session, target, url)
  }

  def deleteRepost(session: UserSession, target: Urn, url: String): Future[Result] = {
    repostsClient.deleteRepost(session, target, url)
  }

  def getReposters(
      session: UserSession,
      target: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[UserRepresentation]] = {
    repostsClient
      .reposters(session, target, pagination.pageSize, pagination.cursor)
      .flatMap { reposts =>
        userRepresentationService
          .getUsers(session, reposts.urns.toSet)
          .map { users =>
            val nextHref = reposts.nextCursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(sortByProvidedUrns(users, reposts.urns).toList, nextHref)
          }
      }
  }

}
