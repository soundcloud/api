package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.service.request.representation.MissingValue
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.trackcoordinator.client.representation.{Error, Errors, Failure, NotFound, Result, Success, Track => CoordinatorTrack, TrackUpdate}
import com.soundcloud.trackcoordinator.client.request.representation.{PublisherTrackUpdate, ScheduleUpdate, TrackMonetizationUpdate}
import com.soundcloud.trackcoordinator.client.TrackCoordinatorClient
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.scalakit.finagle.jsonservice.Params
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.util.Future
import play.api.libs.json.{JsValue, JsObject, Reads}
import com.soundcloud.scalakit.json.Json
import org.joda.time.DateTime


/**
 * Overrides the public api endpoints for editing tracks
 * Reason for overriding is to re-route updating and deleting tracks through
 * track-coordinator, which implements the correct restrictions.
 */
class TracksController(userAuthentication: UserAuthentication,
                       trackCoordinator: TrackCoordinatorClient,
                       mothershipDispatcher: DispatchToMothershipHandler)
    extends BffInjectionBasedController {

  get("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))
  post("/tracks/:trackId")(request => mothershipDispatcher.dispatch(request))

  put("/tracks/:trackId") { request =>
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.updateTrack(session, trackUrn(request), trackUpdateMapper(Json.fromString(request.getContentString) \ "track"), headers(request)).map {
        case Success(track) => render.json(track)
        case NotFound => render.notFound
        case Errors(lst) => renderErrors(lst)
        case _ => render.internalServerError
      }
    }
  }

  delete("/tracks/:trackId") { request =>
    userAuthentication.withLoggedInUser(request) { (session, _) =>
      trackCoordinator.deleteTrack(session, trackUrn(request)).map {
        case Success(()) => render.accepted
        case NotFound => render.notFound
        case Errors(lst) => renderErrors(lst)
        case _ => render.internalServerError
      }
    }
  }

  private def renderErrors(errors: List[Error]) = {
    val status = errors.map(_.status).max
    render.typedJson(errors).status(status)
  }

  private def trackUrn(request: Request): Urn = {
    val IdParamPattern = "(\\d+)".r
    Urn(request.routeParams("urn") match {
          case IdParamPattern(id) => s"soundcloud:tracks:$id"
          case urn => urn
        })
  }

    private def trackUpdateMapper(json: JsValue) = new TrackUpdate(
        monetization = (json \ "monetization").asOpt[JsObject].map(monetization(_)),
        publisher_metadata = (json \ "publisher_metadata").asOpt[JsObject].map(publisherMetadata(_)),
        schedule = schedule(json),
        api_streamable = (json \ "api_streamable").asOpt[Boolean],
        commentable = (json \ "commentable").asOpt[Boolean],
        description = (json \ "description").asOpt[String],
        downloadable = (json \ "downloadable").asOpt[Boolean],
        embeddable = (json \ "embeddable").asOpt[Boolean],
        feedable = (json \ "feedable").asOpt[Boolean],
        genre = (json \ "genre").asOpt[String],
        geo_blockings = (json \ "geo_blockings").asOpt[List[String]],
        label_name = (json \ "label_name").asOpt[String],
        license = (json \ "license").asOpt[String],
        original_filename = (json \ "original_filename").asOpt[String],
        permalink = (json \ "permalink").as[String],
        purchase_title = (json \ "purchase_title").asOpt[String],
        purchase_url = (json \ "purchase_url").asOpt[String],
        release_date = (json \ "release_date").asOpt[String],
        reveal_comments = (json \ "reveal_comments").asOpt[Boolean],
        reveal_stats = (json \ "reveal_stats").asOpt[Boolean],
        sharing = (json \ "sharing").asOpt[String],
        tag_list = (json \ "tag_list").asOpt[String],
        title = (json \ "title").as[String],
        replacing_uid = (json \ "replacing_uid").asOpt[String],
        replacing_original_filename = (json \ "replacing_original_filename").asOpt[String],
        artwork_from_s3 = MissingValue,
        desired_geo_policy_events = None,
        restrictions = (json \ "restrictions").asOpt[Set[String]],
        published_at = MissingValue
    )

  private def monetization(json: JsValue) = new TrackMonetizationUpdate(
    start_timestamp = (json \ "start_timestamp").asOpt[String].map(new DateTime(_)),
    start_timezone = (json \ "start_timezone").asOpt[String],
    end_timestamp = (json \ "end_timestamp").asOpt[String].map(new DateTime(_)),
    end_timezone = (json \ "end_timezone").asOpt[String],
    territories = (json \ "territories").asOpt[List[String]]
  )

  private def publisherMetadata(json: JsValue) = new PublisherTrackUpdate(
    artist = (json \ "artist").asOpt[String],
    album_title = (json \ "album_title").asOpt[String],
    contains_music = (json \ "contains_music").asOpt[Boolean],
    publisher = (json \ "publisher").asOpt[String],
    iswc = (json \ "iswc").asOpt[String],
    upc_or_ean = (json \ "upc_or_ean").asOpt[String],
    isrc = (json \ "isrc").asOpt[String],
    explicit = (json \ "explicit").asOpt[Boolean],
    p_line = (json \ "p_line").asOpt[String],
    c_line = (json \ "c_line").asOpt[String],
    writer_composer = (json \ "writer_composer").asOpt[String],
    release_title = (json \ "release_title").asOpt[String]
  )

  private def schedule(json: JsValue) =
    hasOldSchedule(json).map(oldSchedule(_)) orElse
    hasNewSchedule(json).map(newSchedule(_))


  private def oldSchedule(json: JsValue) = new ScheduleUpdate(
    sunrise = (json \ "scheduled_public_date").asOpt[Long].map(new DateTime(_)),
    sunset = (json \ "scheduled_private_date").asOpt[Long].map(new DateTime(_)),
    timezone = (json \ "scheduled_timezone").asOpt[String].getOrElse(""),
    secret_token_after_sunrise = None,
    schedule_type = "privacy"
  )

  private def newSchedule(json: JsValue) = new ScheduleUpdate(
    sunrise = (json \ "sunrise").asOpt[DateTime](jodaParseReads),
    sunset = (json \ "sunset").asOpt[DateTime](jodaParseReads),
    timezone = (json \ "timezone").asOpt[String].getOrElse(""),
    secret_token_after_sunrise = None,
    schedule_type = "privacy"
  )

  private def hasOldSchedule(json: JsValue) =
    if (json.as[JsObject].keys.contains("scheduled_public_date")) Some(json)
    else None

  private def hasNewSchedule(json: JsValue) = (json \ "schedule").asOpt[JsObject]

  private val jodaParseReads = new Reads[DateTime] {
    def reads(json: JsValue) = json.validate[String].map(DateTime.parse)
  }

  private def headers(request: Request): Params = request.headerMap.iterator.toMap
}
