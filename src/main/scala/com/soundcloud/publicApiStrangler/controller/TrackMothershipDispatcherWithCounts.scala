package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, NonFatal, Try}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

/**
  * This temporary abstraction receives the track json returned by the public api and injects
  * in it the counts that come from Stitch4Counts.
  */
class TrackMothershipDispatcherWithCounts(userAuthentication: UserAuthentication,
                                          mothershipDispatcher: DispatchToMothershipHandler,
                                          stitchClient: StitchClient) {

  def request(request:Request) : Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipDispatcher.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).flatMap(response => {
        lazy val defaultResponse = toResponseBuilder(response).toFuture

        if (response.getStatusCode() < 300) {
          (for {
            responseJson <- Try(Json.parse(response.getContentString())).toOption
            userToTrackUrns = extractUrns(responseJson)
            if userToTrackUrns.nonEmpty
          } yield {
            enrichResponse(session, userToTrackUrns, responseJson, response).map(toResponseBuilder)
          }).getOrElse(defaultResponse)
        } else {
          defaultResponse
        }
      })
    }
  }

  private def enrichResponse(session: UserSession, userToTrackUrn: Set[(Urn, Urn)], responseJson: JsValue, response: Response): Future[Response] = {
    stitchClient.countsForTracks(session, userToTrackUrn).map { counts: Map[Urn, StitchCounts] => {
      responseJson.as[List[JsValue]].map(jsValue => {
        (for {
          (_, trackId) <- getIds(jsValue)
          trackCounts <- counts.get(new Urn("soundcloud", "tracks", trackId.toString))
        } yield {
          jsValue.as[JsObject].deepMerge(Json.obj(
            "playback_count" -> trackCounts.playback_count,
            "comment_count" -> trackCounts.comment_count,
            "download_count" -> trackCounts.download_count,
            "favoritings_count" -> trackCounts.favoritings_count,
            "reposts_count" -> trackCounts.reposts_count
          ))
        }).getOrElse(jsValue)
      })
    } }.handle {
      case NonFatal(_) => responseJson.as[List[JsValue]]
    }.map(newContent => {
      response.setContentString(Json.stringify(new JsArray(newContent)))
      response
    })
  }

  private def extractUrns(json: JsValue): Set[(Urn, Urn)] = {
    json match {
      case JsArray(values) => extractUrnsFromList(values)
      case _ => Set.empty
    }
  }

  private def extractUrnsFromList(values: Seq[JsValue]): Set[(Urn, Urn)] =
    values
      .flatMap(getIds)
      .map {
        case (userId, trackId) => (new Urn("soundcloud", "users", userId.toString), new Urn("soundcloud", "tracks", trackId.toString))
      }.toSet

  private def getIds(jsValue: JsValue): Option[(Int, Int)] = {
    for {
      trackId <- (jsValue \ "id").asOpt[Int]
      userId <- (jsValue \ "user" \ "id").asOpt[Int]
    } yield (userId, trackId)
  }

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.iterator.map {
      case (key, value) => (key, value)
    }.toMap
    new ResponseBuilder()
      .status(response.statusCode)
      .body(response.getContentString())
      .headers(headerMap)
  }
}
