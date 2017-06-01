package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.LoggedInUserSession
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapping.timeline.Timeline
import com.soundcloud.publicApiStrangler.mapping.timeline.e1.TrackTimelineItem
import com.soundcloud.publicApiStrangler.support._
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class TimelineHandler(userAuthentication: UserAuthentication,
                      streamMapper: StreamMapper,
                      activitiesMapper: ActivitiesMapper,
                      publicActivitiesMapper: ActivitiesWithOriginMapper,
                      followingsTracksMapper: FollowingsTracksMapper,
                      pagination: CursorPagination) {

  def renderAllActivities(request: HandlerRequest): Future[Response] = renderActivities(request, activitiesMapper)

  def renderStreamActivities(request: HandlerRequest): Future[Response] = renderActivities(request, streamMapper)

  def renderPublicActivities(request: HandlerRequest): Future[Response] = renderActivities(request, publicActivitiesMapper)

  private def renderActivities(request: HandlerRequest, mapper: TimelineMapper): Future[Response] =
    userAuthentication.withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) =>
        pagination.withPage(request, userUrn) { page =>
          mapper.materialize(session, page).map {
            case Some(info) => JsonResponseBuilder.ok(UntypedJson.write(info.asInstanceOf[Timeline]))
            case None => ResponseBuilder.notFound()
          }
        }
    }

  def renderFollowingsTracks(request: HandlerRequest): Future[Response] = renderFollowingsTracks(request, followingsTracksMapper)

  private def renderFollowingsTracks(request: HandlerRequest, mapper: TimelineMapper): Future[Response] =
    userAuthentication.withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) =>
        pagination.withPage(request, userUrn) { page =>
          mapper.materialize(session, page).map {
            case Some(info) =>
              val tracks = info.collection.map {
                _.asInstanceOf[TrackTimelineItem].track
              }

              if (request.getParam("linked_partitioning", "0") == "1")
                JsonResponseBuilder.ok(UntypedJson.write(Map(
                  "next_href" -> info.nextHref,
                  "collection" -> tracks
                )))
              else
                JsonResponseBuilder.ok(UntypedJson.write(tracks))

            case None => ResponseBuilder.notFound()
          }
        }
    }

}
