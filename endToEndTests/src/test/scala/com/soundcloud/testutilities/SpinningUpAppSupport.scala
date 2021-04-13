package com.soundcloud.testutilities

import com.twitter.finagle
import com.twitter.finagle.Http
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http._
import com.twitter.util.{Await, Duration}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

import java.nio.charset.StandardCharsets

trait SpinningUpAppSupport {
  this: Specification =>

  trait Context extends Scope {
    def server: TestServer
  }

  case class TestServer(host: String, port: Int) {
    val timeout = Duration.fromSeconds(15)
    val serverAddress = s"$host:$port"

    private lazy val client: finagle.Service[Request, Response] = {
      ClientBuilder()
        .stack(Http.client)
        .hosts(serverAddress)
        .hostConnectionLimit(25)
        .build()
    }

    def get(path: String, headers: HeaderMap = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(Method.Get, path, "", headers)

    def post(path: String, body: String, headers: HeaderMap = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(Method.Post, path, body, headers)

    def put(path: String, body: String, headers: HeaderMap = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(Method.Put, path, body, headers)

    def delete(path: String): IntegrationTestHttpResponse =
      executeRequest(Method.Delete, path, "", defaultHeaders)

    def executeRequest(request: Request) =
      new IntegrationTestHttpResponse(Await.result(client(request), timeout))

    protected def executeRequest(method: Method, path: String, body: String, headers: HeaderMap) = {
      val request = Request(Version.Http11, method, path)

      request.headerMap.set("Content-Type", "application/json")
      headers.foreach {
        case (key, value) =>
          request.headerMap.set(key, value)
      }

      request.headerMap.set("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length))
      request.contentString = body

      val response = Await.result(client(request), timeout)
      new IntegrationTestHttpResponse(response)
    }

    private def defaultHeaders = HeaderMap()
  }

  class ServerUnderTestException(reason: String) extends RuntimeException(reason)

  class IntegrationTestHttpResponse(response: Response) {
    lazy val body = response.contentString
    lazy val status = response.status.code
    lazy val headers = response.headerMap
    lazy val location = response.headerMap.get("Location").orNull
    lazy val json = {
      val contentType = response.headerMap.get("Content-Type").get
      if (contentType.startsWith(MediaType.Json)) {
        Json.parse(body)
      } else {
        throw new ServerUnderTestException(s"Invalid content type in response: $contentType")
      }
    }
  }
}
