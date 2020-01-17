package com.soundcloud.publicApiStrangler.handler

import java.util

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.soundcloud.jvmkit.module.telemetry.{Counter, Telemetry}
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.exp.MultipartDecoder
import com.twitter.util.{Future, Return, Throw, Try}
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._

class TokenExchangeHandler(
    mothershipDispatch: Handler,
    metrics: TokenExchangeHandler.Metrics,
    parseRequest: HandlerRequest => TokenExchangeHandler.TokenExchangeRequest.ParseResult =
      TokenExchangeHandler.TokenExchangeRequest.parse
) {
  def instrumentedMothershipDispatch(
      request: HandlerRequest
  ): Future[Response] = {
    instrument(request)(mothershipDispatch)
  }

  private def instrument(
      request: HandlerRequest
  )(
      handler: Handler
  ): Future[Response] = {
    handler(request).foreach(response =>
      parseRequest(request) match {
        case Right(tokenExchangeRequest) =>
          metrics.grantTypeCounter
            .labels(
              tokenExchangeRequest.accessGrant.typeName,
              response.statusCode.toString
            )
            .inc()
        case Left(error) =>
          metrics.requestErrorCounter
            .labels(error.labelValue, response.statusCode.toString)
            .inc()
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
      telemetry.counter(
        "oauth_token_exchange_error",
        "OAuth 2 Token exchange error.",
        "error",
        "response_status"
      )
  }

  case class TokenExchangeRequest(
      clientCredentials: TokenExchangeRequest.ClientCredentials,
      accessGrant: TokenExchangeRequest.AccessGrant
  )

  object TokenExchangeRequest {
    case class ClientCredentials(clientId: String, clientSecret: String)

    sealed class AccessGrant(val typeName: String)
    case class AuthorizationCode(code: String, redirectUri: String) extends AccessGrant("authorization_code")
    case class ResourceOwnerPasswordCredentials(username: String, password: String) extends AccessGrant("password")
    case class RefreshToken(refreshToken: String) extends AccessGrant("refresh_token")
    case object ClientCredentialsGrant extends AccessGrant("client_credentials")

    abstract sealed class AccessGrantReader(val name: String) {
      def read(parameters: SingleValuedParameters): Option[AccessGrant]
    }
    val AuthorizationCodeReader: AccessGrantReader = new AccessGrantReader(
      "authorization_code"
    ) {
      override def read(parameters: SingleValuedParameters): Option[AccessGrant] = {
        (
          parameters.get("code"),
          parameters.get("redirect_uri")
        ) match {
          case (Some(code), Some(redirectUri)) =>
            Some(AuthorizationCode(code, redirectUri))
          case _ => None
        }
      }
    }
    val ResourceOwnerPasswordCredentialsReader: AccessGrantReader =
      new AccessGrantReader("password") {
        override def read(parameters: SingleValuedParameters): Option[AccessGrant] = {
          (
            parameters.get("username"),
            parameters.get("password")
          ) match {
            case (Some(username), Some(password)) =>
              Some(ResourceOwnerPasswordCredentials(username, password))
            case _ => None
          }
        }
      }
    val RefreshTokenReader: AccessGrantReader = new AccessGrantReader(
      "refresh_token"
    ) {
      override def read(parameters: SingleValuedParameters): Option[AccessGrant] = {
        parameters.get("refresh_token").map(RefreshToken)
      }
    }
    val ClientCredentialsGrantReader: AccessGrantReader = new AccessGrantReader(
      "client_credentials"
    ) {
      override def read(parameters: SingleValuedParameters): Option[AccessGrant] =
        Some(ClientCredentialsGrant)
    }

    object AccessGrantReader {
      val All: Seq[AccessGrantReader] = Seq(
        AuthorizationCodeReader,
        ResourceOwnerPasswordCredentialsReader,
        RefreshTokenReader,
        ClientCredentialsGrantReader
      )

      def from(string: String): Option[AccessGrantReader] = {
        All.find(_.name == string)
      }
    }

    type ParseResult = Either[RequestError, TokenExchangeRequest]
    def parse(
        request: HandlerRequest
    ): ParseResult = {
      Try {
        val parameters = parseRequestBody(request)
        (readClientCredentials(parameters), readAccessGrant(parameters)) match {
          case (Right(cc), Right(ag)) => Right(TokenExchangeRequest(cc, ag))
          // The error from reading the access grant takes precedence.
          case (_, Left(agError)) => Left(agError)
          case (Left(ccError), _) => Left(ccError)
        }
      } match {
        case Return(r) => r
        case Throw(_) => Left(UnparseableRequestBody)
      }
    }

    type Parameters = Map[String, Seq[String]]
    type SingleValuedParameters = Map[String, String]
    private def parseRequestBody(request: HandlerRequest): SingleValuedParameters = {
      (request.contentType match {
        case Some(mediaType) if mediaType.matches("multipart\\/.*") =>
          decodeMultipart(request)
        case _ =>
          decodeFormUrlEncoded(request)
      }).mapValues(_.headOption).collect { case (key, Some(value)) => (key, value) }
    }

    private def readClientCredentials(
        parameters: SingleValuedParameters
    ): Either[RequestError, ClientCredentials] = {
      (
        parameters.get("client_id"),
        parameters.get("client_secret")
      ) match {
        case (Some(clientId), Some(clientSecret)) =>
          Right(ClientCredentials(clientId, clientSecret))
        case _ => Left(InvalidRequest("missing_client_credentials"))
      }
    }

    private def readAccessGrant(
        parameters: SingleValuedParameters
    ): Either[RequestError, AccessGrant] = {
      selectAccessGrantReader(
        parameters.get("grant_type")
      ).flatMap(_.read(parameters) match {
        case Some(a) => Right(a)
        case None => Left(InvalidRequest("incomplete_grant_information"))
      })
    }

    private def selectAccessGrantReader(
        maybeString: Option[String]
    ): Either[RequestError, AccessGrantReader] = {
      maybeString match {
        case Some(string: String) =>
          AccessGrantReader.from(string) match {
            case Some(accessGrantReader) => Right(accessGrantReader)
            case None => Left(UnsupportedGrantType)
          }
        case None => Left(InvalidRequest("missing_grant_type"))
      }
    }

    private def decodeFormUrlEncoded(request: HandlerRequest): Parameters = {
      def immutableScalaMap(
          javaMap: util.Map[String, util.List[String]]
      ): Parameters = {
        javaMap.asScala.mapValues(_.asScala.toSeq).toMap
      }

      immutableScalaMap(
        new QueryStringDecoder(request.contentString, false).getParameters
      )
    }

    private def decodeMultipart(request: HandlerRequest): Parameters = {
      MultipartDecoder
        .decode(request)
        .map(_.attributes.map { case (k, v) => k -> v.seq })
        .getOrElse(Map.empty)
    }

    sealed abstract class RequestError(val name: String) {
      def labelValue: String = name
    }
    case class InvalidRequest(val reason: String) extends RequestError("invalid_request") {
      override def labelValue: String = s"${super.labelValue}:${reason}"
    }
    case object UnsupportedGrantType extends RequestError("unsupported_grant_type")
    case object UnparseableRequestBody extends RequestError("unparseable_request_body")
  }

}
