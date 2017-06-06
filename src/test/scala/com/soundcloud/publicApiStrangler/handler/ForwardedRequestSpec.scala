package com.soundcloud.publicApiStrangler.handler

import java.io.InputStream
import java.net.InetSocketAddress

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Duration, Future}
import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut}
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.specs2.mutable.BeforeAfter

class ForwardedRequestSpec extends UnitSpecification {

  trait Context extends BeforeAfter {
    val server = new MockWebServer()
    val client = Http.client.withStreaming(enabled = false).newService(s"localhost:${server.getPort}")

    def stranglerService: Service[Request, Response] =
      new Service[Request, Response] {
        def apply(request: Request): Future[Response] = {
          client.apply(ForwardedRequest(request))
        }
      }

    val stranglerServer = Http.server.withStreaming(true).serve(new InetSocketAddress(0), stranglerService)
    val stranglerServerPort = stranglerServer.boundAddress.asInstanceOf[InetSocketAddress].getPort
    val stranglerClient = Http.client.newService(s"localhost:$stranglerServerPort")

    override def before: Any = {

    }

    override def after: Any = {
      server.shutdown()
      Await.result(stranglerServer.close(Duration.fromMilliseconds(500)))
      Await.result(stranglerClient.close(Duration.fromMilliseconds(500)))
      Await.result(client.close(Duration.fromMilliseconds(500)))
    }
  }

  "properly forwards GET request" in new Context {
    server.enqueue(new MockResponse().setBody("donkey"))

    val request = new HttpGet(s"http://localhost:$stranglerServerPort/tracks")
    request.addHeader("X-Favourite-Animal", "zebra")
    val httpclient = HttpClients.createDefault()
    val response = httpclient.execute(request)
    response.getStatusLine.getStatusCode ==== 200
    EntityUtils.toString(response.getEntity) ==== "donkey"

    val recordedRequest = server.takeRequest()
    recordedRequest.getMethod ==== "GET"
    recordedRequest.getPath ==== "/tracks"
    recordedRequest.getBody.readUtf8() ==== ""
    recordedRequest.getHeader("Host") ==== "api.soundcloud.com"
    recordedRequest.getHeader("X-Forwarded-Proto") ==== "https"
    recordedRequest.getHeader("Transfer-Encoding") ==== null
    recordedRequest.getHeader("Content-Length") ==== null
    recordedRequest.getHeader("X-Favourite-Animal") ==== "zebra"
  }

  "properly forwards POST request" in new Context {
    server.enqueue(new MockResponse().setBody("donkey"))

    val request = new HttpPost(s"http://localhost:$stranglerServerPort/tracks")
    request.addHeader("X-Favourite-Animal", "zebra")
    request.setEntity(new StringEntity("giraffe"))
    val httpclient = HttpClients.createDefault()
    val response = httpclient.execute(request)
    response.getStatusLine.getStatusCode ==== 200
    EntityUtils.toString(response.getEntity) ==== "donkey"

    val recordedRequest = server.takeRequest()
    recordedRequest.getMethod ==== "POST"
    recordedRequest.getPath ==== "/tracks"
    recordedRequest.getBody.readUtf8() ==== "giraffe"
    recordedRequest.getHeader("Host") ==== "api.soundcloud.com"
    recordedRequest.getHeader("X-Forwarded-Proto") ==== "https"
    recordedRequest.getHeader("Transfer-Encoding") ==== null
    recordedRequest.getHeader("Content-Length") ==== "7"
    recordedRequest.getHeader("X-Favourite-Animal") ==== "zebra"
  }

  "properly forwards PUT request" in new Context {
    server.enqueue(new MockResponse().setBody("donkey"))

    val request = new HttpPut(s"http://localhost:$stranglerServerPort/tracks")
    request.addHeader("X-Favourite-Animal", "zebra")
    request.setEntity(new StringEntity("giraffe"))
    val httpclient = HttpClients.createDefault()
    val response = httpclient.execute(request)
    response.getStatusLine.getStatusCode ==== 200
    EntityUtils.toString(response.getEntity) ==== "donkey"

    val recordedRequest = server.takeRequest()
    recordedRequest.getMethod ==== "PUT"
    recordedRequest.getPath ==== "/tracks"
    recordedRequest.getBody.readUtf8() ==== "giraffe"
    recordedRequest.getHeader("Host") ==== "api.soundcloud.com"
    recordedRequest.getHeader("X-Forwarded-Proto") ==== "https"
    recordedRequest.getHeader("Transfer-Encoding") ==== null
    recordedRequest.getHeader("Content-Length") ==== "7"
    recordedRequest.getHeader("X-Favourite-Animal") ==== "zebra"
  }

  "properly forwards POST request with Connection: close" in new Context {
    server.enqueue(new MockResponse().setBody("donkey"))

    val request = new HttpPost(s"http://localhost:$stranglerServerPort/tracks")
    request.addHeader("X-Favourite-Animal", "zebra")
    request.addHeader("Connection", "close")
    request.setEntity(new StringEntity("giraffe"))
    val httpclient = HttpClients.createDefault()
    val response = httpclient.execute(request)
    response.getStatusLine.getStatusCode ==== 200
    EntityUtils.toString(response.getEntity) ==== "donkey"


    val recordedRequest = server.takeRequest()
    recordedRequest.getMethod ==== "POST"
    recordedRequest.getPath ==== "/tracks"
    recordedRequest.getBody.readUtf8() ==== "giraffe"
    recordedRequest.getHeader("Host") ==== "api.soundcloud.com"
    recordedRequest.getHeader("X-Forwarded-Proto") ==== "https"
    recordedRequest.getHeader("Transfer-Encoding") ==== null
    recordedRequest.getHeader("Content-Length") ==== "7"
    recordedRequest.getHeader("X-Favourite-Animal") ==== "zebra"
    recordedRequest.getHeader("Connection") ==== "close"
  }


  "sends multipart POST requests as chunked" in new Context {
    server.enqueue(new MockResponse().setBody("okey dokey"))

    // Create stream
    val streamLength = 10
    val inputStream: InputStream = new InputStream {
      var count = 0

      def read: Int = {
        count += 1
        if (count > streamLength)
          -1
        else
          '\u0000'
      }
    }

    // Build request
    val request = new HttpPost(s"http://localhost:$stranglerServerPort/tracks")
    val reqEntity = MultipartEntityBuilder.create()
      .addBinaryBody("track[asset_data]", inputStream, ContentType.APPLICATION_OCTET_STREAM, "donkey_song.mp3")
      .build()
    request.setEntity(reqEntity)

    // Make request
    val httpclient = HttpClients.createDefault()
    val response = httpclient.execute(request)
    response.getStatusLine.getStatusCode ==== 200
    EntityUtils.toString(response.getEntity) ==== "okey dokey"

    // Verify
    val recordedRequest = server.takeRequest()
    recordedRequest.getHeader("Transfer-Encoding") ==== "chunked"
    recordedRequest.getHeader("Content-Length") ==== null
    recordedRequest.getHeader("Host") ==== "api.soundcloud.com"
    recordedRequest.getHeader("X-Forwarded-Proto") ==== "https"
    val requestBody = recordedRequest.getBody.readUtf8()
    requestBody.contains("\r\nContent-Disposition: form-data; name=\"track[asset_data]\"; filename=\"donkey_song.mp3\"\r\n") ==== true
    requestBody.contains("\r\nContent-Type: application/octet-stream\r\n") ==== true
    requestBody.contains("\r\nContent-Transfer-Encoding: binary\r\n") ==== true
    requestBody.contains("\r\n\r\n" + ("\u0000" * 10) + "\r\n--") ==== true
  }
}
