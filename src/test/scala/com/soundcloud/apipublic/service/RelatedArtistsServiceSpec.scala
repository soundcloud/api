package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.SystemPlaylistsClient
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.mapper.similarcreators.SimilarCreators
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.{UserBuilder, UserRepresentationsService}
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

import java.net.URL

class RelatedArtistsServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val seedUser = Urn("soundcloud", "users", "99")
    val relatedUrn1 = Urn("soundcloud", "users", "10")
    val relatedUrn2 = Urn("soundcloud", "users", "20")

    val systemPlaylistsClient = mock[SystemPlaylistsClient]
    val userRepresentationsService = mock[UserRepresentationsService]

    val relatedArtistsService = new RelatedArtistsService(userRepresentationsService, systemPlaylistsClient)

    val user1: UserRepresentation = new UserBuilder().setUrn(relatedUrn1).build
    val user2: UserRepresentation = new UserBuilder().setUrn(relatedUrn2).build

    val pagination = RelatedArtistsPagination(
      Some(10),
      Some(0),
      false,
      new URL("https://api.soundcloud.com/users/99/related?limit=10&offset=0")
    )
  }

  "#relatedArtists" >> {
    "returns related users when they exist" in new Context {
      when(systemPlaylistsClient.fetchSimilarCreators(session, seedUser, 50))
        .thenReturn(Future(Some(SimilarCreators(List(relatedUrn1, relatedUrn2)))))
      when(userRepresentationsService.users(session, List(relatedUrn1, relatedUrn2), false))
        .thenReturn(Future(List(user1, user2)))

      val result =
        Await.result(relatedArtistsService.relatedArtists(session, seedUser, pagination))

      result must beSome[Collection[UserRepresentation]]
      result.get.items ==== List(user1, user2)
    }

    "returns None when system playlists returns none" in new Context {
      when(systemPlaylistsClient.fetchSimilarCreators(session, seedUser, 50))
        .thenReturn(Future(None))

      Await.result(relatedArtistsService.relatedArtists(session, seedUser, pagination)) must beNone
    }

    "returns Some(empty collection) when user representations are empty" in new Context {
      when(systemPlaylistsClient.fetchSimilarCreators(session, seedUser, 50))
        .thenReturn(Future(Some(SimilarCreators(List(relatedUrn1)))))
      when(userRepresentationsService.users(session, List(relatedUrn1), false))
        .thenReturn(Future(List.empty))

      val result = Await.result(relatedArtistsService.relatedArtists(session, seedUser, pagination))

      result must beSome[Collection[UserRepresentation]]
      result.get.items ==== List.empty
    }
  }
}
