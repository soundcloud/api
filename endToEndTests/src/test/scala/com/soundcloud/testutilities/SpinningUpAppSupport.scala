package com.soundcloud.testutilities

import java.net.URL
import com.soundcloud.scalakit.finagle.http.{SuccessfulStatusClass, StatusCode}
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http._
import com.twitter.util.{Await, Duration}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import java.nio.charset.StandardCharsets



trait SpinningUpAppSupport { this: Specification =>

  trait Context extends Scope {
    def server: TestServer
  }

  case class TestServer(host: String, port: Int) {
    val timeout = Duration.fromSeconds(15)
    val serverAddress = s"$host:$port"
    val serverUrl = s"http://$serverAddress"
    val healthEndpointUrl = new URL(serverUrl + "/-/health")

    private lazy val client: finagle.Service[Request, Response] = {
      ClientBuilder()
        .codec(Http())
        .hosts(serverAddress)
        .hostConnectionLimit(1)
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
      headers.foreach {
        case (key, value) =>
          request.headerMap.add(key, value)
      }
      request.headerMap.add("Content-Type", "application/json")
      request.headerMap.add("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length))
      request.contentString = body

      val response = Await.result(client(request), timeout)
      new IntegrationTestHttpResponse(response)
    }

    private def defaultHeaders = HeaderMap()
  }

  def dockerBasedHost: String = {
    val host = sys.env.get("SERVER_HOST")
    if (host.isEmpty)
      throw new IllegalStateException("SERVER_HOST env variable not found.")
    host.get
  }

  class ServerUnderTestException(reason: String) extends RuntimeException(reason)

  class IntegrationTestHttpResponse(response: Response) {
    lazy val body = response.contentString
    lazy val status = response.status.code
    lazy val headers = response.headerMap
    lazy val location = response.headerMap.get("Location").getOrElse(null)
    lazy val json = {
      if (new StatusCode(response.status.code).statusClass == SuccessfulStatusClass) {
        val contentType = response.headerMap.get("Content-Type").get
        if (contentType.startsWith(MediaType.Json)) {
          Json.fromString(body)
        } else {
          throw new ServerUnderTestException(s"Invalid content type in response: $contentType")
        }
      } else {
        throw new ServerUnderTestException(s"Invalid response. Status: ${response.status}. Body: $body")
      }
    }
  }
}

