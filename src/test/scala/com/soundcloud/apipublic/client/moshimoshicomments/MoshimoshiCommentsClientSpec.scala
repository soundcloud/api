package com.soundcloud.apipublic.client.moshimoshicomments

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.service.pagination._
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.twitter.finagle.http.{ParamMap, Response}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class MoshimoshiCommentsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val service = mock[JsonClient]
    val session = mock[UserSession]
    val client = new MoshimoshiCommentsClient(service)

    val track = Urn("soundcloud", "tracks", "123")
    val path = Path() / "tracks" / track.identifier / "comments"
    val serviceParams = Params("linked_partitioning" -> "1")
    val pagination = mock[OffsetBasedPagination]

    def stubService(response: Response) = {
      when(pagination.getParams).thenReturn(ParamMap())
      when(service.getWithSession(session, path, serviceParams, Headers.empty))
        .thenReturn(Future.value(response))
    }
  }

  "#fetchTrackComments" >> {
    "Returns not found for a not found response" in new Context {
      stubService(ResponseBuilder.notFound())
      Await.result(client.fetchTrackComments(session, track, pagination)) ==== NotFound().bad
    }

    "Returns not valid for a bad request response" in new Context {
      stubService(ResponseBuilder.badRequest())
      Await.result(client.fetchTrackComments(session, track, pagination)) ==== NotValid("").bad
    }

    "Returns internal error for a unhandled response exception" in new Context {
      stubService(ResponseBuilder.serviceUnavailable())
      Await.result(client.fetchTrackComments(session, track, pagination)) ==== HttpServiceError(HttpResponseFields(503)).bad
    }

    trait SuccessContext extends Context {
      val moshimoshiJson = Fixtures.moshiComments
      val response = ResponseBuilder.ok(Json.stringify(moshimoshiJson))
      stubService(response)
    }

    "Returns a comments page on success" in new SuccessContext {
      Await.result(client.fetchTrackComments(session, track, pagination)) ==== moshimoshiJson
        .as[MoshimoshiCommentsPagedResponse]
        .good
    }
  }
}
