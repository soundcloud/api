package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures

class CreateLikeResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val status: Status
    val json: String = ""

    def response: Response = {
      val newResponse = Response(status)
      newResponse.setContentString(json)
      newResponse
    }

    def result: CreateLikeResponse = CreateLikeResponseMapper(response)
  }

  "when a new like is created" >> {
    trait LikeCreatedContext extends Context {
      override val status = Status.Created
      override val json = Fixtures.lieblingLikeCreationSuccess
    }

    "returns a LikeCreated" in new LikeCreatedContext {
      result ==== LikeCreated
    }
  }

  "when the like already exists" >> {
    trait LikeAlreadyExistsContext extends Context {
      override val status = Status.Ok
      override val json = Fixtures.lieblingLikeCreationSuccess
    }

    "returns a LikeAlreadyExists" in new LikeAlreadyExistsContext {
      result ==== LikeAlreadyExists
    }
  }

  "when the user is blocked" >> {
    trait UserBlockedContext extends Context {
      override val status = Status.Forbidden
    }

    "returns a LikeCreated" in new UserBlockedContext {
      result ==== UserBlocked
    }
  }

  "when the user has a spam warning" >> {
    trait UserHasSpamWarningContext extends Context {
      override val status = Status.TooManyRequests
      override val json = Json.stringify(Json.obj("spam_warning_urn" -> "soundcloud:spam-warnings:42"))
    }

    "returns a UserHasSpamWarnings" in new UserHasSpamWarningContext {
      result ==== UserHasSpamWarning
    }
  }

  "when the likeable is not found" >> {
    trait LikeableNotFoundContext extends Context {
      override val status = Status.NotFound
    }

    "returns a LikeableNotFound" in new LikeableNotFoundContext {
      result ==== LikeableNotFound
    }
  }

  "when the URN is not valid" >> {
    trait LikeableNotFoundContext extends Context {
      override val status = Status.UnprocessableEntity
    }

    "returns a UrnNotValid" in new LikeableNotFoundContext {
      result ==== UrnNotValid
    }
  }
}
