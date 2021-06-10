package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.trackCoordinatorTrack
import com.twitter.finagle.http.{Response, Status}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class TrackCoordinatorCreateMapperSpec extends Specification {
  "on success" >> {
    "returns track" in new Scope {
      val successResponse = Json.stringify(trackCoordinatorTrack)
      val response = Response(Status.Created)
      response.setContentString(successResponse)

      val result = TrackCoordinatorCreateMapper(response)
      val expectedResult = trackCoordinatorTrack.as[TrackCoordinatorTrack]
      result match {
        case Good(track) => track ==== expectedResult
        case _ => ko
      }
    }
  }

  "on not found" >> {
    "returns 404" in new Scope {
      val notFoundResponse = """{"code":404,"message": "404 not found"}""".trim
      val response = Response(Status.NotFound)
      response.setContentString(notFoundResponse)

      val result = TrackCoordinatorCreateMapper(response)
      result ==== Bad(NotFound())
    }
  }

  "on error" >> {
    "correctly maps error response" in new Scope {
      val errorResponse =
        """{"status":"400 - Bad Request","errors":[{"message":"user Primary email address must be confirmed before you can upload", "error_type": "e"}]}""".trim
      val response = Response(Status.BadRequest)
      response.setContentString(errorResponse)

      val result = TrackCoordinatorCreateMapper(response)

      result match {
        case Bad(NotValid(msg)) =>
          msg.head mustEqual "user Primary email address must be confirmed before you can upload"
        case _ => ko
      }
    }

    "returns invalid request if cannot parse error at all" in new Scope {
      val errorResponse = """"{u cannot parse me}""".trim

      val response = Response.apply(Status.BadRequest)
      response.setContentString(errorResponse)

      val result = TrackCoordinatorCreateMapper(response)

      result match {
        case Bad(NotValid(msg)) => msg.head mustEqual "invalid request"
        case _ => ko
      }
    }

    "returns internal error if unhandled exception" in new Scope {
      val errorResponse = """{"code":500,"message": "internal server error"}""".trim

      val response = Response.apply(Status.InternalServerError)
      response.setContentString(errorResponse)

      val result = TrackCoordinatorCreateMapper(response)

      result match {
        case Bad(HttpServiceError(responseFields)) => responseFields.statusCode mustEqual 500
        case _ => ko
      }
    }
  }

}
