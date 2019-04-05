package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}

class TrackAccessRecorderClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val moshimoshiClient = mock[JsonClient]
    val client = new TrackAccessRecorderClient(moshimoshiClient)
    val session = mock[UserSession]
    val trackUrn = Urn("soundcloud", "tracks", "1234")
    val shouldLog = true

    lazy val result = Await.result(client.recordAccess(session, trackUrn, "stream", shouldLog))
  }

  "#recordAccess" >> {
    "when Moshimoshi returns a 200 response" >> {
      trait OkContext extends Context {
        moshimoshiClient.getWithSession(session, Path() / "tracks" / trackUrn / "access" / "stream", Params.empty, Headers.empty) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "returns 200" in new OkContext {
        result.status ==== Status.Ok
      }
    }

    "when Moshimoshi returns a non-200 response" >> {
      trait BadContext extends Context {
        moshimoshiClient.getWithSession(session, Path() / "tracks" / trackUrn / "access" / "stream", Params.empty, Headers.empty) returns
          Future.value(ResponseBuilder().status(Status.BadRequest).build)
      }

      "returns the same response" in new BadContext {
        result.status ==== Status.BadRequest
      }
    }

    "when stream access should not be logged" >> {
      trait DontLogContext extends Context {
        override val shouldLog = false
        moshimoshiClient.getWithSession(session, Path() / "tracks" / trackUrn / "access" / "stream", Params("skip_logging" -> "1"), Headers.empty) returns
          Future.value(ResponseBuilder().status(Status.Ok).build)
      }

      "calls Moshimoshi with 'skip_logging' param" in new DontLogContext {
        result.status ==== Status.Ok
      }
    }
  }
}
