package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.service.RelatedArtistsService
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future
import org.mockito.Mockito.when

class RelatedArtistsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val userAuthentication = new FakeUserAuthentication(session)

    val relatedArtistsService = mock[RelatedArtistsService]
    val baseUrl = "https://api.soundcloud.com"

    val relatedArtistsHandler =
      new RelatedArtistsHandler(userAuthentication, relatedArtistsService, baseUrl)

    val seedUserUrn = Urn("soundcloud", "users", "99")

    override def routingDefinitions = Routing.forRelatedArtistsHandler(relatedArtistsHandler)

    val relatedUser: UserRepresentation = new UserBuilder().setUrn(Urn("soundcloud", "users", "10")).build
    val usersCollection = Collection(List(relatedUser), None)
  }

  "processes requests to /users/:id/related" in new Context {
    val paginationParams = "?limit=1&offset=2"
    val path = s"/users/99/related"

    when(relatedArtistsService.relatedArtists(===(session), ===(seedUserUrn), any()))
      .thenReturn(Future(Some(usersCollection)))

    val response = get(path + paginationParams)
    response.statusCode ==== 200
  }

  "returns 404 when service returns none" in new Context {
    val paginationParams = "?limit=1&offset=2"
    val path = "/users/99/related"

    when(relatedArtistsService.relatedArtists(===(session), ===(seedUserUrn), any()))
      .thenReturn(Future(None))

    val response = get(path + paginationParams)
    response.statusCode ==== 404
  }
}
