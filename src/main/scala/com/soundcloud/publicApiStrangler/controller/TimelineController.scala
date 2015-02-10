package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.support._

class TimelineController(
                          userAuthentication: UserAuthentication,
                          streamMapper: StreamMapper,
                          activitiesMapper: ActivitiesMapper,
                          publicActivitiesMapper: ActivitiesWithOriginMapper,
                          pagination: CursorPagination
                          ) extends BffInjectionBasedController {

  // Android & iPad specific
  get("/e1/me/activities")(doMagic(_, activitiesMapper))
  get("/e1/me/activities.json")(doMagic(_, activitiesMapper))
  get("/e1/me/stream")(doMagic(_, streamMapper))
  get("/e1/me/stream.json")(doMagic(_, streamMapper))

  get("/me/activities")(doMagic(_, publicActivitiesMapper))
  get("/me/activities.json")(doMagic(_, publicActivitiesMapper))

  // deprecated functionality, aliased to /me/activities
  get("/me/activities/")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/track")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/tracks")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/tracks/")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/tracks.json")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/tracks/:tag")(doMagic(_, publicActivitiesMapper)) // /affiliated, /exclusive
  get("/me/activities/tracks/:tag.json")(doMagic(_, publicActivitiesMapper)) // /affiliated.xml, /exclusive.json
  get("/me/activities/all")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/all.json")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/all/own")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/all/own.json")(doMagic(_, publicActivitiesMapper))


  private def doMagic(request: BffRequest, mapper: TimelineMapper) =
    userAuthentication.withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) =>
        pagination.withPage(request, userUrn) { page =>
          mapper.materialize(session, page).map {
            case Some(info) => render.json(info)
            case None => render.notFound
          }.map(_.headers(DefaultResponseHeaders.defaultHeaders))
        }
    }

}
