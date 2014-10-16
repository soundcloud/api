package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.web.BffController
import com.soundcloud.jvmkit.{LoggedInUserSession, Urn}
import com.soundcloud.publicApiStrangler.mapper.{EntityMapper, StreamMapper}
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

  lazy val entityMapper = new EntityMapper(okidokiClient, lieblingClient, baseUrl)
  lazy val streamMapper = new StreamMapper(timelineClient, entityMapper)

  val fallback = new DispatchToMothershipHandler(publicApiClient)

  get("/e1/me/stream") {
    request =>
      withLoggedInUser(request) {
        (session: LoggedInUserSession, userUrn: Urn) => {
          if (rollingOut(session)) {
            val page = pageFor(request, userUrn)
            streamMapper.materialize(session, page).map {
              case Some(info) => render.json(info)
              case None => render.notFound
            }
          } else {
            fallbackToMothership(request)
          }
        }
      }
  }

  // TODO !!!! turn this off for full rollout !!!!
  val testUsers = List("8478647", "69099281")

  private def rollingOut(session: LoggedInUserSession) = {
    testUsers.contains(session.getUser.getIdentifier)
  }

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
