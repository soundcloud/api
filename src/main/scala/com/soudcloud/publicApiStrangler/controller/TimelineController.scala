package com.soudcloud.publicApiStrangler.controller

import com.soudcloud.publicApiStrangler.mapper.{EntityMapper, StreamMapper}
import com.soudcloud.publicApiStrangler.support.CursorPagination
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.service.component.{OkidokiComponent, TimelineComponent}

trait TimelineController extends BffController
    with TimelineComponent
    with OkidokiComponent
    with CursorPagination {

  // TODO content filtering

  lazy val entityMapper = new EntityMapper(okidokiClient)
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
