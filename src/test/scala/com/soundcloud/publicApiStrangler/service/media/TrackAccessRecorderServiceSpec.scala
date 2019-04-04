package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Method, Request, Status}
import com.twitter.util.{Await, Future}

class TrackAccessRecorderServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackAccessRecorderClient = mock[TrackAccessRecorderClient]
    val service = new TrackAccessRecorderService(trackAccessRecorderClient)
    val session = mock[UserSession]
    val trackUrn = Urn("soundcloud", "tracks", "1234")

    val reqMethod: Method = Method.Get
    val range: Option[String] = None

    lazy val request = {
      val req = Request(reqMethod, "http://local/")
      range.foreach(v => req.headerMap.add("Range", v))
      HandlerRequest(req)
    }

    val action = ResponseBuilder().status(Status.Ok).body("foobar").build
    lazy val result = Await.result(service.recordStreamAccess(session, request, trackUrn)(action))
  }

  "#recordStreamAccess" >> {
    "when client returns a 200 response" >> {
      trait OkContext extends Context {
        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, true) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "action is executed and returned" in new OkContext {
        result.status ==== Status.Ok
        result.contentString ==== "foobar"
      }
    }

    "when client returns a non-200 response" >> {
      trait BadContext extends Context {
        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, true) returns
          Future.value(ResponseBuilder().status(Status.BadRequest).build)
      }

      "action is executed and returned" in new BadContext {
        result.status ==== Status.BadRequest
        result.contentString ==== ""
      }
    }

    "when request method is not GET" >> {
      trait MethodNotGetContext extends Context {
        override val reqMethod = Method.Head

        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, false) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "action is executed and play is not logged" in new MethodNotGetContext {
        result.status ==== Status.Ok
        result.contentString ==== "foobar"
      }
    }

    "when Range header is not set" >> {
      trait NoRangeHeaderContext extends Context {
        override val range = None

        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, true) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "action is executed and play is logged" in new NoRangeHeaderContext {
        result.status ==== Status.Ok
        result.contentString ==== "foobar"
      }
    }

    "when Range header is set to first byte" >> {
      trait FirstByteRangeContext extends Context {
        override val range = Some("bytes=0-100")

        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, true) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "action is executed and play is logged" in new FirstByteRangeContext {
        result.status ==== Status.Ok
        result.contentString ==== "foobar"
      }
    }

    "when Range header is set to none-zero byte" >> {
      trait NoneZeroByteRangeContext extends Context {
        override val range = Some("bytes=200-400")

        trackAccessRecorderClient.recordStreamAccess(session, trackUrn, false) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "action is executed and play is not logged" in new NoneZeroByteRangeContext {
        result.status ==== Status.Ok
        result.contentString ==== "foobar"
      }
    }
  }
}
