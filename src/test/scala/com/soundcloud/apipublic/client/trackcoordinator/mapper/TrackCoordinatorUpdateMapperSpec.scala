package com.soundcloud.apipublic.client.trackcoordinator.mapper

import com.soundcloud.apipublic.client.trackcoordinator.TrackCoordinatorTrack
import com.soundcloud.apipublic.test.fixtures.Fixtures.trackCoordinatorTrack
import com.twitter.finagle.http.{Response, Status}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.apipublic.client.support.UnhandledResponseException

class TrackCoordinatorUpdateMapperSpec extends Specification {
  "on success" >> {
    "returns track" in new Scope {
      val successResponse = Json.stringify(trackCoordinatorTrack)
      val response = Response(Status.Ok)
      response.setContentString(successResponse)

      val result = TrackCoordinatorUpdateMapper(response)
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

      val result = TrackCoordinatorUpdateMapper(response)
      result ==== Bad(NotFound())
    }
  }

  "on error" >> {
    "correctly maps error response" in new Scope {
      val errorResponse =
        """{"status":"400 - Bad Request","errors":[{"message":"user Primary email address must be confirmed before you can upload", "error_type": "e"}]}""".trim
      val response = Response(Status.BadRequest)
      response.setContentString(errorResponse)

      val result = TrackCoordinatorUpdateMapper(response)

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

      val result = TrackCoordinatorUpdateMapper(response)

      result match {
        case Bad(NotValid(msg)) => msg.head mustEqual "invalid request"
        case _ => ko
      }
    }
  }

  "on unhandled response" >> {
    "throws unhandled exception" in new Scope {
      val errorResponse = """{"code":500,"message": "internal server error"}""".trim

      val response = Response.apply(Status.InternalServerError)
      response.setContentString(errorResponse)

      TrackCoordinatorUpdateMapper(response) must throwA[UnhandledResponseException]
    }
  }
}
