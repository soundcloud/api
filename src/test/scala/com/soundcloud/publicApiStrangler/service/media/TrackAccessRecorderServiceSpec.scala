package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Method, Request, Status}
import com.twitter.util.{Await, Future}

class TrackAccessRecorderServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val trackAccessRecorderClient = mock[TrackAccessRecorderClient]
    val fakeTelemetry = Telemetry.createIsolatedInstance
    val service = new TrackAccessRecorderService(trackAccessRecorderClient, fakeTelemetry)
    val session = new UserSessionBuilder().build
    val trackUrn = Urn("soundcloud", "tracks", "1234")

    val reqMethod: Method = Method.Get
    val range: Option[String] = None
    val secretToken: Option[String] = None
    val loggingEnabled: Boolean = true

    lazy val request = {
      val req = Request(reqMethod, "http://local/" + secretToken.map(token => s"?secret_token=$token").getOrElse(""))
      range.foreach(v => req.headerMap.set("Range", v))
      HandlerRequest(req)
    }

    val action = ResponseBuilder().status(Status.Ok).body("foobar").build

    lazy val resultStream =
      Await.result(service.recordStreamAccess(session, request, trackUrn, loggingEnabled)(Future.value(action)))
    lazy val resultDownload =
      Await.result(service.recordDownloadAccess(session, request, trackUrn)(Future.value(action)))
  }

  Seq("stream", "download").foreach { accessType =>
    trait AccessTypeContext extends Context {
      lazy val result = accessType match {
        case "stream" => resultStream
        case "download" => resultDownload
      }
    }

    s"#record${accessType.capitalize}Access" >> {
      "when client returns a 200 response" >> {
        trait OkContext extends AccessTypeContext {
          trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, true, secretToken) returns
            Future.value(ResponseBuilder().status(Status.Ok).build)
        }

        "action is executed and returned" in new OkContext {
          result.status ==== Status.Ok
          result.contentString ==== "foobar"
        }

        "with a secret token" >> {
          trait WithSecretTokenContext extends AccessTypeContext {
            override val secretToken = Some("only4me")

            trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, true, secretToken) returns
              Future.value(ResponseBuilder().status(Status.Ok).build)
          }

          "action is executed and returned" in new WithSecretTokenContext {
            result.status ==== Status.Ok
            result.contentString ==== "foobar"
          }
        }
      }

      "when client returns a non-200 response" >> {
        trait BadContext extends AccessTypeContext {
          trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, true, secretToken) returns
            Future.value(ResponseBuilder().status(Status.BadRequest).build)
        }

        "action is not executed and returned" in new BadContext {
          result.status ==== Status.BadRequest
          result.contentString ==== ""
        }
      }

      "when Range header is not set" >> {
        trait NoRangeHeaderContext extends AccessTypeContext {
          override val range = None

          trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, true, secretToken) returns
            Future.value(ResponseBuilder().status(Status.Ok).build)
        }

        "action is executed and play is logged" in new NoRangeHeaderContext {
          result.status ==== Status.Ok
          result.contentString ==== "foobar"
        }
      }

      "when Range header is set to first byte" >> {
        trait FirstByteRangeContext extends AccessTypeContext {
          override val range = Some("bytes=0-100")

          trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, true, secretToken) returns
            Future.value(ResponseBuilder().status(Status.Ok).build)
        }

        "action is executed and play is logged" in new FirstByteRangeContext {
          result.status ==== Status.Ok
          result.contentString ==== "foobar"
        }
      }

      "when Range header is set to none-zero byte" >> {
        trait NoneZeroByteRangeContext extends AccessTypeContext {
          override val range = Some("bytes=200-400")

          trackAccessRecorderClient.recordAccess(session, trackUrn, accessType, false, secretToken) returns
            Future.value(ResponseBuilder().status(Status.Ok).build)
        }

        "action is executed and play is not logged" in new NoneZeroByteRangeContext {
          result.status ==== Status.Ok
          result.contentString ==== "foobar"
        }
      }

      "when loggingEnabled is set to false" >> {
        trait LoggingDisabledContext extends Context {
          override val loggingEnabled = false

          trackAccessRecorderClient.recordAccess(session, trackUrn, "stream", false, secretToken) returns
            Future.value(ResponseBuilder().status(Status.Ok).build)
        }

        "action is executed and play is not logged" in new LoggingDisabledContext {
          val result = resultStream
          result.status ==== Status.Ok
          result.contentString ==== "foobar"
        }
      }
    }
  }

  "when recording access for download" >> {
    trait DownloadContext extends Context {
      trackAccessRecorderClient.recordAccess(session, trackUrn, accessFor = "download", shouldLog = true, secretToken) returns
        Future.value(ResponseBuilder().status(Status.Ok).build)

      def accessWasRecorded =
        (there was one(trackAccessRecorderClient).recordAccess(
          session,
          trackUrn,
          accessFor = "download",
          shouldLog = true,
          secretToken
        )).isSuccess
    }

    "when request method is GET" >> {
      "download is logged" in new DownloadContext {
        override val reqMethod: Method = Method.Get

        resultDownload.status ==== Status.Ok
        accessWasRecorded ==== true
      }
    }

    "when request method is HEAD" >> {
      "download is logged" in new DownloadContext {
        override val reqMethod: Method = Method.Head

        resultDownload.status ==== Status.Ok
        accessWasRecorded ==== true
      }
    }
  }

  "when recording access for stream" >> {
    trait StreamContext extends Context {
      val shouldLog: Boolean

      trackAccessRecorderClient.recordAccess(===(session), ===(trackUrn), ===("stream"), any[Boolean], ===(secretToken)) returns
        Future.value(ResponseBuilder().status(Status.Ok).build)

      def accessWasRecorded =
        (there was one(trackAccessRecorderClient).recordAccess(
          session,
          trackUrn,
          accessFor = "stream",
          shouldLog,
          secretToken
        )).isSuccess
    }

    "when request method is GET" >> {
      "play is logged" in new StreamContext {
        override val reqMethod: Method = Method.Get
        override val shouldLog = true

        resultStream.status ==== Status.Ok
        accessWasRecorded ==== true
      }
    }

    "when request method is HEAD" >> {
      "play is not logged" in new StreamContext {
        override val reqMethod: Method = Method.Head
        override val shouldLog = false

        resultStream.status ==== Status.Ok
        accessWasRecorded ==== true
      }
    }
  }
}
