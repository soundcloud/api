package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.TrackTimelineItem
import com.soundcloud.publicApiStrangler.support._
import play.api.libs.json.Json

class TimelineController(
                          userAuthentication: UserAuthentication,
                          streamMapper: StreamMapper,
                          activitiesMapper: ActivitiesMapper,
                          publicActivitiesMapper: ActivitiesWithOriginMapper,
                          followingsTracksMapper: FollowingsTracksMapper,
                          pagination: CursorPagination
                          ) extends BffInjectionBasedController {

  // Android & iPad specific
  get("/e1/me/activities")(renderActivities(_, activitiesMapper))
  get("/e1/me/activities.json")(renderActivities(_, activitiesMapper))
  get("/e1/me/stream")(renderActivities(_, streamMapper))
  get("/e1/me/stream.json")(renderActivities(_, streamMapper))

  get("/me/activities")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities.json")(renderActivities(_, publicActivitiesMapper))

  // deprecated functionality, aliased to /me/activities
  get("/me/activities/")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/track")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/tracks")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/tracks/")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/tracks.json")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/tracks/:tag")(renderActivities(_, publicActivitiesMapper)) // /affiliated, /exclusive
  get("/me/activities/tracks/:tag.json")(renderActivities(_, publicActivitiesMapper)) // /affiliated.xml, /exclusive.json
  get("/me/activities/all")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/all.json")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/all/own")(renderActivities(_, publicActivitiesMapper))
  get("/me/activities/all/own.json")(renderActivities(_, publicActivitiesMapper))

  // For the IFTTT integration
  get("/me/followings/tracks")(renderFollowingsTracks(_, followingsTracksMapper))

  private def renderActivities(request: BffRequest, mapper: TimelineMapper) =
    userAuthentication.withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) =>
        pagination.withPage(request, userUrn) { page =>
          mapper.materialize(session, page).map {
            case Some(info) => render.json(info)
            case None => render.notFound
          }
        }
    }

  private def renderFollowingsTracks(request: BffRequest, mapper: TimelineMapper) =
    userAuthentication.withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) =>
        pagination.withPage(request, userUrn) { page =>
          mapper.materialize(session, page).map {
            case Some(info) =>
              val tracks = info.collection.map {
                _.asInstanceOf[TrackTimelineItem].track
              }

              if (request.getParam("linked_partitioning", "0") == "1")
                render.json(Map(
                  "next_href" -> info.nextHref,
                  "collection" -> tracks
                ))
              else
                render.json(tracks)

            case None => render.notFound
          }
        }
    }

}
