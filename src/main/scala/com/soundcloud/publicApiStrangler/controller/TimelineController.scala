package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.mapper.{EntityMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.support.{TrackFiltering, DispatchToMothershipHandler, CursorPagination}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.service.component.{LieblingComponent, OkidokiComponent, TimelineComponent}

trait TimelineController extends BffController
    with TimelineComponent
    with OkidokiComponent
    with LieblingComponent
    with CursorPagination {

  lazy val entityMapper = new EntityMapper(okidokiClient, lieblingClient, baseUrl)
  lazy val streamMapper = new StreamMapper(timelineClient, entityMapper)

  get("/e1/me/stream") {
    request =>
      withLoggedInUser(request) {
        (session: LoggedInUserSession, userUrn: Urn) => {
          val page = pageFor(request, userUrn)
          streamMapper.materialize(session, page).map {
            case Some(info) => render.json(info)
            case None => render.notFound
          }
        }
      }
  }

}
