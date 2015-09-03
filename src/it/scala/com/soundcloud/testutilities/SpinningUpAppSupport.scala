package com.soundcloud.testutilities

import java.net.URL
import com.soundcloud.scalakit.finagle.http.{SuccessfulStatusClass, StatusCode}
import com.soundcloud.scalakit.json.Json
import com.twitter.finagle
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{MediaType, Http}
import com.twitter.util.{Await, Duration}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import scala.sys.process.Process

trait SpinningUpAppSupport { this: Specification =>

  trait Context extends Scope {
    def server: TestServer
  }

  case class TestServer(host: String, port: Int) {
    val timeout = Duration.fromSeconds(15)
    val serverAddress = s"$host:$port"
    val serverUrl = s"http://$serverAddress"
    val healthEndpointUrl = new URL(serverUrl + "/-/health")

    private lazy val client: finagle.Service[HttpRequest, HttpResponse] = {
      ClientBuilder()
        .codec(Http())
        .hosts(serverAddress)
        .hostConnectionLimit(1)
        .build()
    }

    def get(path: String, headers: HttpHeaders = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(HttpMethod.GET, path, "", headers)

    def post(path: String, body: String, headers: HttpHeaders = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(HttpMethod.POST, path, body, headers)

    def put(path: String, body: String, headers: HttpHeaders = defaultHeaders): IntegrationTestHttpResponse =
      executeRequest(HttpMethod.PUT, path, body, headers)

    def delete(path: String): IntegrationTestHttpResponse =
      executeRequest(HttpMethod.DELETE, path, "", defaultHeaders)

    protected def executeRequest(method: HttpMethod, path: String, body: String, headers: HttpHeaders) = {
      val request = new DefaultHttpRequest(HTTP_1_1, method, path)
      request.headers().add(headers)
      request.headers().add(CONTENT_TYPE, "application/json")
      val buffer = ChannelBuffers.copiedBuffer(body, UTF_8)
      request.headers().add(CONTENT_LENGTH, String.valueOf(buffer.readableBytes()))
      request.setContent(buffer)

      val response = Await.result(client(request), timeout)
      new IntegrationTestHttpResponse(response)
    }

    private def defaultHeaders = new DefaultHttpHeaders(true)
  }

  def dockerBasedHost = sys.env.getOrElse("SERVER_HOST", Process("docker-ip", None, "NO_AUTO_UPDATE" -> "1").!!.trim)

  class ServerUnderTestException(reason: String) extends RuntimeException(reason)

  class IntegrationTestHttpResponse(response: HttpResponse) {
    lazy val body = response.getContent.toString(UTF_8)
    lazy val status = response.getStatus.getCode
    lazy val headers = response.headers()
    lazy val location = response.headers().get(LOCATION)
    lazy val json = {
      if (new StatusCode(response.getStatus.getCode).statusClass == SuccessfulStatusClass) {
        val contentType = response.headers().get(CONTENT_TYPE)
        if (contentType.startsWith(MediaType.Json)) {
          Json.fromString(body)
        } else {
          throw new ServerUnderTestException(s"Invalid content type in response: $contentType")
        }
      } else {
        throw new ServerUnderTestException(s"Invalid response. Status: ${response.getStatus}. Body: $body")
      }
    }
  }
}

