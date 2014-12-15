package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request => BffRequest, ResponseBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.publicApiStrangler.mapper.timeline._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.soundcloud.service.component.{LieblingComponent, OkidokiComponent, TimelineComponent}
import com.twitter.finagle.http.Request
import scala.collection.JavaConversions._

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
  get("/e1/me/activities")(doMagic(_, activitiesMapper))
  get("/e1/me/activities.json")(doMagic(_, activitiesMapper))
  get("/e1/me/stream")(doMagic(_, streamMapper))
  get("/e1/me/stream.json")(doMagic(_, streamMapper))


  private def doMagic(request: BffRequest, mapper: TimelineMapper) = {
    withLoggedInUser(request) {
      (session: LoggedInUserSession, userUrn: Urn) => {
        if(rollingOut(session)) {
          val page = pageFor(request, userUrn)
          mapper.materialize(session, page).map {
            case Some(info) => render.json(info)
            case None => render.notFound
          }.map(_.headers(defaultHeaders))
        } else {
          fallbackToMothership(request)
        }
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



  def rollingOut(session: LoggedInUserSession) = true

//  private val percent = 50.0
//  private val percentRollout = (1.0 / percent) * 100.0
//  def rollingOut(session: LoggedInUserSession) = (session.getUser.getIdentifier.toInt % percentRollout).toInt == 0

  // TODO !!!! turn this off for full rollout !!!!
//  val testUsers = List("8478647", "69099281", "1074292", "258229", "172720", "1196384", "107637429", "36587595", "21592204", "67278483", "50881634", "5725061", "1717170", "25882813")
//  private def rollingOut(session: LoggedInUserSession) = testUsers.contains(session.getUser.getIdentifier)

  private def fallbackToMothership(request: Request) = {
    fallback.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).map {
      response =>
        new ResponseBuilder().
          body(response.getContentString()).
          status(response.getStatusCode()).
          headers(response.headers().entries.map(e => e.getKey -> e.getValue).toMap)
    }
  }

}
