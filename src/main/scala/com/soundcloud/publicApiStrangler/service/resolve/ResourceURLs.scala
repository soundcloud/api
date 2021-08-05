package com.soundcloud.publicApiStrangler.service.resolve

import io.lemonlabs.uri.config.{ExcludeNones, UriConfig}
import io.lemonlabs.uri.dsl._
import io.lemonlabs.uri.{Host, Url}

import scala.util.{Failure, Success, Try}

object ResourceURLs {
  private val WWW_HOST = "soundcloud.com"
  private val WWW_ROOT = s"https://$WWW_HOST"

  private val USER_PERMALINK_URL_PARSER = """^/([^/]+)$""".r
  private val TRACK_PERMALINK_URL_PARSER = """^/([^/]+)/([^/]+)(?:/([^/]+))?$""".r
  private val PLAYLIST_PERMALINK_URL_PARSER = """^/([^/]+)/sets/([^/]+)(?:/([^/]+))?$""".r

  sealed trait ResourceURL {
    def normalized: String
  }

  sealed trait PermalinkURL extends ResourceURL {
    val secretToken: Option[String]
  }
  case class UserPermalinkUrl(usernameSlug: String) extends PermalinkURL {
    override def normalized = WWW_ROOT / usernameSlug
    val secretToken = None
  }
  case class TrackPermalinkUrl(usernameSlug: String, trackSlug: String, secretToken: Option[String])
      extends PermalinkURL {
    override def normalized = withSecretTokenAsPathPart(WWW_ROOT / usernameSlug / trackSlug, secretToken)
  }
  case class PlaylistPermalinkUrl(usernameSlug: String, playlistSlug: String, secretToken: Option[String])
      extends PermalinkURL {
    override def normalized = withSecretTokenAsPathPart(WWW_ROOT / usernameSlug / "sets" / playlistSlug, secretToken)
  }

  def parsePermalinkUrl(permalinkUrl: String): Try[PermalinkURL] = {
    val parsedPermalinkUrl: Url = Url.parse(permalinkUrl)
    val secretTokenParam = parsedPermalinkUrl.query.paramMap.get("secret_token").map(_.head)

    parsedPermalinkUrl.hostOption match {
      case Some(Host(WWW_HOST)) =>
        parsedPermalinkUrl.path.toStringRaw match {
          case PLAYLIST_PERMALINK_URL_PARSER(usernamePart, playlistPart, "") =>
            Success(PlaylistPermalinkUrl(usernamePart, playlistPart, None))
          case PLAYLIST_PERMALINK_URL_PARSER(usernamePart, playlistPart, secretTokenPart) => {
            val maybeToken = if (secretTokenPart != null) Option(secretTokenPart) else secretTokenParam
            Success(PlaylistPermalinkUrl(usernamePart, playlistPart, maybeToken))
          }
          case TRACK_PERMALINK_URL_PARSER(usernamePart, trackPart, "") =>
            Success(TrackPermalinkUrl(usernamePart, trackPart, secretTokenParam))
          case TRACK_PERMALINK_URL_PARSER(usernamePart, trackPart, secretTokenPart) => {
            val maybeToken = if (secretTokenPart != null) Option(secretTokenPart) else secretTokenParam
            Success(TrackPermalinkUrl(usernamePart, trackPart, maybeToken))
          }
          case USER_PERMALINK_URL_PARSER(usernamePart) => Success(UserPermalinkUrl(usernamePart))
          case _ => Failure(new IllegalArgumentException(s"Permalink $permalinkUrl did not match known patterns"))
        }
      case _ => Failure(new IllegalArgumentException(s"Permalink $permalinkUrl did not match known host patterns"))
    }
  }

  private def withSecretTokenAsPathPart(url: Url, secretToken: Option[String]): String =
    secretToken match {
      case Some(secretTokenString) => url / secretTokenString
      case _ => url
    }

  implicit val config: UriConfig = UriConfig(renderQuery = ExcludeNones)
}
