package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsNull, Json}

class TracksClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val userSession = mock[UserSession]
    val client = new TracksClient(jsonClient)

    val trackUrn = Urn("soundcloud", "tracks", "2")
    val trackUid = Some("NnPYWvWwB6ln")
    val apiStreamable = Some(true)
    val contentAuth = new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
  }

  "visibleTrack" >> {
    "track service returns a track" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(List(
          Json.obj("urn" -> trackUrn.toString)
        ))
      )
      jsonClient.postWithSession(userSession, Path() / "tracks", Params.empty, Headers.empty, Some(Json.stringify(jsonBody))) returns
        Future.value(jsonResponse(Status.Ok, withContentsOf("tracks", "visible_track")))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) ==== Some(VisibleTrack(trackUrn, trackUid, apiStreamable, contentAuth))
    }

    "track service returns no track" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(List(
          Json.obj("urn" -> trackUrn.toString)
        ))
      )
      jsonClient.postWithSession(userSession, Path() / "tracks", Params.empty, Headers.empty, Some(Json.stringify(jsonBody))) returns
        Future.value(jsonResponse(Status.Ok, Json.obj("data" -> JsArray())))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) ==== None
    }

    "track service returns a 500" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(List(
          Json.obj("urn" -> trackUrn.toString)
        ))
      )
      jsonClient.postWithSession(userSession, Path() / "tracks", Params.empty, Headers.empty, Some(Json.stringify(jsonBody))) returns
        Future.value(jsonResponse(Status.InternalServerError, JsNull))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) should throwAn[UnhandledResponseException]
    }
  }

}
