package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.service.component.{LieblingComponent, OkidokiComponent, TimelineComponent}

trait TimelineController extends BffController
    with TimelineComponent
    with OkidokiComponent
    with LieblingComponent
    with PublicApiClientComponent
    with CursorPagination {

  lazy val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, baseUrl)
  lazy val entityMapper = new EntityMapper(okidokiClient, lieblingClient, baseUrl, entitySummaryMapper)
  lazy val streamMapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
  lazy val activitiesMapper = new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper)
  lazy val publicActivitiesMapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)

  val fallback = new DispatchToMothershipHandler(publicApiClient)

  get("/me/activities")(doMagic(_, publicActivitiesMapper))
  get("/me/activities.json")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/all")(doMagic(_, publicActivitiesMapper))
  get("/me/activities/all.json")(doMagic(_, publicActivitiesMapper))
//  get("/e1/me/activities")(doMagic(_, activitiesMapper))
//  get("/e1/me/activities.json")(doMagic(_, activitiesMapper))
//  get("/e1/me/stream")(doMagic(_, streamMapper))
//  get("/e1/me/stream.json")(doMagic(_, streamMapper))


  private def doMagic(request: BffRequest, mapper: TimelineMapper) = {
    withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) => {
        val page = pageFor(request, userUrn)
        mapper.materialize(session, page).map {
          case Some(info) => render.json(info)
          case None => render.notFound
        }.map(_.headers(defaultHeaders))
      }
    }
  }


  private val defaultHeaders = Map(
    "Access-Control-Allow-Headers" -> "Accept, Authorization, Content-Type, Origin",
    "Access-Control-Allow-Methods" -> "GET, PUT, POST, DELETE",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Expose-Headers"-> "Date",
    "Cache-Control" -> "private, max-age=0, must-revalidate"
  )

}
