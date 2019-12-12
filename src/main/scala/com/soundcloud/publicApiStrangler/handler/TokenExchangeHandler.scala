package com.soundcloud.publicApiStrangler.handler

import java.util

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.{Counter, Telemetry}
import com.twitter.finagle.http.{MediaType, Response}
import com.twitter.util.{Future, Return, Throw, Try}
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._

class TokenExchangeHandler(mothershipDispatch: Handler, metrics: TokenExchangeHandler.Metrics) {
  def instrumentedMothershipDispatch(request: HandlerRequest): Future[Response] = {
    instrument(request)(mothershipDispatch)
  }

  private def instrument(request: HandlerRequest)(handler: Handler): Future[Response] = {
    val tokenExchangeRequest = TokenExchangeHandler.Request(request)

    handler(request).foreach(
      response =>
        tokenExchangeRequest match {
          case Right(r) =>
            metrics.grantTypeCounter.labels(r.grantType.name, response.statusCode.toString).inc()
          case Left(e) =>
            metrics.requestErrorCounter.labels(e.name, response.statusCode.toString).inc()
        }
    )
  }
}

object TokenExchangeHandler {
  class Metrics(telemetry: Telemetry) {
    val grantTypeCounter: Counter =
      telemetry.counter(
        "oauth_token_exchange_grant_type",
        "OAuth 2 Token exchange request grant type.",
        "grant_type",
        "response_status"
      )
    val requestErrorCounter: Counter =
      telemetry.counter("oauth_token_exchange_error", "OAuth 2 Token exchange error.", "error", "response_status")
  }

  // This class is intended to hold the full access token request in the future.
  case class Request(
      grantType: Request.AccessGrantType
  )

  object Request {
    sealed class AccessGrantType(val name: String)
    val AuthorizationCode = new AccessGrantType("authorization_code")
    val ResourceOwnerPasswordCredentials = new AccessGrantType("password")
    val RefreshToken = new AccessGrantType("refresh_token")
    val ClientCredentials = new AccessGrantType("client_credentials")

    object AccessGrantType {
      val All = Seq(AuthorizationCode, ResourceOwnerPasswordCredentials, RefreshToken, ClientCredentials)
      def from(string: String): Option[AccessGrantType] = {
        All.find(_.name == string)
      }
    }

    def apply(request: HandlerRequest): Either[RequestError, Request] = {
      Try {
        request.contentType match {
          case Some(MediaType.WwwForm) =>
            val parameters = decodeParameters(request)
            getGrantType(parameters).map(Request(_))
          case Some(mediaType) if mediaType.matches("multipart\\/.*") =>
            Left(NonStandardMultipartContentType(mediaType.replaceAll("; .*", "")))
          case Some(_) => Left(UnsupportedContentType)
          case None => Left(MissingContentType)
        }
      } match {
        case Return(r) => r
        case Throw(e) => Left(UnexpectedError(e))
      }
    }

    type Parameters = util.Map[String, util.List[String]]
    private def getGrantType(parameters: Parameters): Either[RequestError, AccessGrantType] = {
      Option(parameters.get("grant_type")).flatMap(_.asScala.headOption) match {
        case Some(string: String) =>
          AccessGrantType.from(string) match {
            case Some(accessGrant) => Right(accessGrant)
            case None => Left(UnsupportedGrantType)
          }
        case None => Left(InvalidRequest)
      }
    }
    private def decodeParameters(request: HandlerRequest): Parameters = {
      new QueryStringDecoder(request.contentString, false).getParameters
    }

    sealed abstract class RequestError(val name: String)
    case object InvalidRequest extends RequestError("invalid_request")
    case object UnsupportedGrantType extends RequestError("unsupported_grant_type")
    case object MissingContentType extends RequestError("missing_content_type")
    case object UnsupportedContentType extends RequestError("unsupported_content_type")
    case class NonStandardMultipartContentType(mediaType: String)
        extends RequestError(s"non_standard_multipart_content_type: $mediaType")
    case class UnexpectedError(throwable: Throwable) extends RequestError("unexpected_error")
  }
}
