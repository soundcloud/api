package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.{Response, Status}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class UpdatePlaylistArtworkResponseMapperSpec extends Specification {
  trait Context extends Scope {
    val updatePlaylistArtworkResponseMapper = new UpdatePlaylistArtworkResponseMapper
    val response: Response
    def updatedPlaylistArtworkResponse: Outcome[Unit] = updatePlaylistArtworkResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val response = Response(Status.Created)
    }

    "returns ok" in new SuccessContext {
      updatedPlaylistArtworkResponse === Good(())
    }
  }

  "when unauthorized response" >> {
    trait UnauthorizedContext extends Context {
      override val response = Response(Status.Unauthorized)
    }

    "returns unauthorized" in new UnauthorizedContext {
      updatedPlaylistArtworkResponse === Bad(NotAuthorized())
    }
  }

  "when forbidden response" >> {
    trait ForbiddenContext extends Context {
      override val response = Response(Status.Forbidden)
    }

    "returns not allowed" in new ForbiddenContext {
      updatedPlaylistArtworkResponse === Bad(NotAllowed())
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val response = Response(Status.NotFound)
    }

    "returns not found" in new NotFoundContext {
      updatedPlaylistArtworkResponse === Bad(NotFound("Playlist not found"))
    }
  }

  "when bad request" in {
    trait BadRequestContext extends Context {
      override val response = Response(Status.BadRequest)
    }

    "returns not valid" in new BadRequestContext {
      updatedPlaylistArtworkResponse === Bad(NotValid("Bad request"))
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val response = Response(Status.BadGateway)
    }

    "throws exception" in new InvalidContext {
      updatedPlaylistArtworkResponse must throwA[UnhandledResponseException]
    }
  }
}
