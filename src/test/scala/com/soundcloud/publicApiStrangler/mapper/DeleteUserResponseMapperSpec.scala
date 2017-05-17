package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.representation.{InvalidParametersDeleteUserResponse, OkDeleteUserResponse, UserNotFoundDeleteUserResponse}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsNull, Json}

class DeleteUserResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val deleteUserResponseMapper = new DeleteUserResponseMapper
    val responseStatus: Status

    lazy val response = JsonResponseBuilder().status(responseStatus).body(Json.stringify(JsNull)).build
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val responseStatus = Status.Ok
    }

    "returns ok" in new SuccessContext {
      deleteUserResponseMapper(response) ==== OkDeleteUserResponse
    }
  }

  "when bad reqeust response" >> {
    trait BadRequestContext extends Context {
      override val responseStatus = Status.BadRequest
    }

    "returns invalid params" in new BadRequestContext {
      deleteUserResponseMapper(response) ==== InvalidParametersDeleteUserResponse
    }
  }

  "when unprocessable entity response" >> {
    trait UnprocessableEntityContext extends Context {
      override val responseStatus = Status.UnprocessableEntity
    }

    "returns invalid params" in new UnprocessableEntityContext {
      deleteUserResponseMapper(response) ==== InvalidParametersDeleteUserResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val responseStatus = Status.NotFound
    }

    "returns invalid urn" in new NotFoundContext {
      deleteUserResponseMapper(response) ==== UserNotFoundDeleteUserResponse
    }
  }

  "when conflict response" >> {
    trait ConflictContext extends Context {
      override val responseStatus = Status.Conflict
    }

    "returns invalid urn" in new ConflictContext {
      deleteUserResponseMapper(response) must throwAn[UnhandledResponseException]
    }
  }
}
