package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.media.MediaUrlsRepository
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{MalformedUrnException, Urn}
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.soundcloud.publicApiStrangler.policies.{ContentAuthorization, ContentPolicy}
import com.twitter.finagle.http.{MediaType, Method, Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Forwards stream requests to public api but intervene in case content policy for track is SNIP
  * or BLOCK.
  *
  * We still forward request to public-api because it has support for play restrictions which we
  * do not support yet in bff apps or authsy.
  */
class TrackStreamHandler(mothershipDispatcher: DispatchToMothershipHandler,
                         contentAuthRules: ContentAuthorizationRules,
                         mediaUrlsRepository: MediaUrlsRepository) {

  def handle(request: HandlerRequest, userSession: UserSession, mapper: TrackStreamResponseMapper): Future[Response] =
    Try(new Urn("soundcloud", "tracks", request.routeParams("trackId"))) match {
      case Return(trackUrn) =>
        if (urnWithNumericIdentifier(trackUrn)) {
          val responses = Future.join(mothershipDispatcher.dispatch(request), contentAuthFor(userSession, trackUrn))
          responses.flatMap {
            case (mothershipResponse: Response, contentAuth: ContentAuthorization) =>
              if (mothershipResponse.statusCode < 400) {
                contentAuth.getPolicy match {
                  case ContentPolicy.ALLOW | ContentPolicy.MONETIZE => Future.value(mothershipResponse)
                  case ContentPolicy.SNIP => replaceStream(request, userSession, trackUrn, contentAuth, mapper, isHttpsRequest(request))
                  case ContentPolicy.BLOCK if (userSession.isAnonymous) => Future.value(generateResponseFor(request, HttpResponseStatus.UNAUTHORIZED))
                  case ContentPolicy.BLOCK => Future.value(generateResponseFor(request, HttpResponseStatus.FORBIDDEN))
                }
              } else {
                Future.value(mothershipResponse)
              }
          }
        } else {
          Future.value(generateResponseFor(request, HttpResponseStatus.NOT_FOUND))
        }
      case Throw(exception) => exception match {
        case ex: MalformedUrnException => Future.value(generateResponseFor(request, HttpResponseStatus.NOT_FOUND))
        case _ => Future.value(generateResponseFor(request, HttpResponseStatus.INTERNAL_SERVER_ERROR))
      }
    }

  private def isHttpsRequest(request: HandlerRequest): Boolean =
    request.headerMap.get("x-forwarded-proto") match {
      case Some(protocol) => protocol.toLowerCase() == "https"
      case _ => false
    }

  private def contentAuthFor(session: UserSession, trackUrn: Urn): Future[ContentAuthorization] =
    contentAuthRules.fetchRules(session, Seq(trackUrn)).map(ca => ca.head)

  private def replaceStream(request: HandlerRequest, session: UserSession, trackUrn: Urn, contentAuth: ContentAuthorization, mapper: TrackStreamResponseMapper, useHttps: Boolean = false): Future[Response] = {
    val mediaUrls = mediaUrlsRepository.byUrn(session, trackUrn, contentAuth, useHttps)
    val isHeadRequest = request.method == Method.Head
    mapper.map(mediaUrls, isHeadRequest)
  }

  private def urnWithNumericIdentifier(urn: Urn) = urn.getIdentifier.matches("\\d+")

  private def generateResponseFor(request: HandlerRequest, status: HttpResponseStatus) = {
    val builder = ResponseBuilder().
      status(Status(status.getCode)).
      header("Status", status.getCode + " " + status.getReasonPhrase).
      header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z")))

    if (request.method != Method.Head) {
      builder.
        mediaType(MediaType.Json).
        body("{\"errors\":[{\"error_message\":\"" + status.getCode + " - " + status.getReasonPhrase + "\"}]}").
        build
    } else {
      builder.build
    }

  }
}
