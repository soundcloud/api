package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{Method, Status}
import com.twitter.util.Future
import org.specs2.mock.Mockito
import play.api.libs.json.{JsNull, JsValue, Json}

object Helpers extends Mockito {
  def expectOkResponse(path: Path, expected: JsValue, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.Ok, ExpectedBody(expected))

  def expectBadRequestResponse(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.BadRequest)

  def expectForbiddenResponse(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.Forbidden)

  def expectNotFoundResponse(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.NotFound)

  def expectUnauthorizedResponse(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.Unauthorized)

  def expectInternalErrorResponse(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, Status.InternalServerError)

  def expectResponseCode(status: Status)(path: Path, params: Params = Params.empty, headers: Headers = Headers.empty)(
      implicit service: JsonClient,
      session: UserSession
  ) =
    expectResponse(path, params, Method.Get, headers, status)

  def expectResponse(
      path: Path,
      params: Params,
      method: Method,
      headers: Headers,
      code: Status,
      expectedBody: MockedBody = new ExpectedBody(JsNull, None)
  )(implicit service: JsonClient, session: UserSession) = {
    val bodyString = expectedBody.requestBodyString

    (method match {
      case Method.Get => service.getWithSession(session, path, params, headers)
      case Method.Post => service.postWithSession(session, path, params, headers, bodyString)
      case Method.Patch => service.patchWithSession(session, path, params, headers, bodyString)
      case Method.Delete => service.deleteWithSession(session, path, params, headers, bodyString)
      case Method.Put => service.putWithSession(session, path, params, headers, bodyString)
      case Method.Options => service.options(session, path, params, headers, bodyString)
    }) returns Future(JsonResponseBuilder().status(code).body(Json.stringify(expectedBody.responseBody)).build)
  }

  trait MockedBody {
    val responseBody: JsValue

    def requestBodyString: Option[String]
  }

  case class ExpectedBody(responseBody: JsValue = JsNull, requestBody: Option[JsValue] = None) extends MockedBody {
    override def requestBodyString = requestBody.map(Json.stringify)
  }

  case class ExpectedBodyWithRawRequest(responseBody: JsValue = JsNull, requestBodyString: Option[String] = None)
      extends MockedBody {}
}
