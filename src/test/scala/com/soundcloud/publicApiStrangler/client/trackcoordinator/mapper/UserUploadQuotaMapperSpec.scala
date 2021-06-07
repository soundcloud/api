package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.client.trackcoordinator.UserUploadQuotaMapper
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.trackCoordinatorUploadQuota
import com.twitter.finagle.http.{Response, Status}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class UserUploadQuotaMapperSpec extends Specification {
  "on success" >> {
    "returns upload quota" in new Scope {
      val successResponse = Json.stringify(trackCoordinatorUploadQuota)
      val response = Response(Status.Ok)
      response.setContentString(successResponse)

      val result = UserUploadQuotaMapper(response)
      result match {
        case Good(quota) => {
          quota.seconds_used ==== 196
          quota.seconds_limit ==== Some(21600)
        }
        case _ => ko
      }
    }
  }

  "on not found" >> {
    "returns 404" in new Scope {
      val notFoundResponse = """{"code":404,"message": "404 not found"}""".trim
      val response = Response(Status.NotFound)
      response.setContentString(notFoundResponse)

      val result = UserUploadQuotaMapper(response)
      result ==== Bad(NotFound())
    }
  }

  "on error" >> {
    "correctly maps error response" in new Scope {
      val errorResponse =
        """{"status":"400 - Bad Request","errors":[{"message":"user Primary email address must be confirmed before you can upload", "error_type": "e"}]}""".trim
      val response = Response(Status.BadRequest)
      response.setContentString(errorResponse)

      val result = UserUploadQuotaMapper(response)

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

      val result = UserUploadQuotaMapper(response)

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

      UserUploadQuotaMapper(response) must throwA[UnhandledResponseException]
    }
  }
}
