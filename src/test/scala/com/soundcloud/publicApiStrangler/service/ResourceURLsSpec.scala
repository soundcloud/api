package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.service.resolve.ResourceURLs
import com.soundcloud.publicApiStrangler.service.resolve.ResourceURLs.{
  PlaylistPermalinkUrl,
  TrackPermalinkUrl,
  UserPermalinkUrl
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification

import scala.util.Success

class ResourceURLsSpec extends UnitSpecification {
  trait Context extends Scope {
    val trackUrn = Urn("soundcloud", "tracks", "1")
  }

  "#permalink" >> {
    "with a user permalink" >> {
      trait UserContext extends Context {
        val userPermalink = "http://soundcloud.com/some-user"
      }

      "creates user permalink" in new UserContext {
        ResourceURLs.parsePermalinkUrl(userPermalink) ==== Success(UserPermalinkUrl("some-user"))
      }
    }

    "with a track permalink" >> {
      trait PlaylistContext extends Context {
        val playlistPermalink = "http://soundcloud.com/some-user/a-track"
      }

      "with NO secret token" in new PlaylistContext {
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          TrackPermalinkUrl("some-user", "a-track", None)
        )
      }

      "with a secret token as path param" in new PlaylistContext {
        override val playlistPermalink = "http://soundcloud.com/some-user/a-track/thesecret"
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          TrackPermalinkUrl("some-user", "a-track", Some("thesecret"))
        )
      }

      "with a secret token as query param" in new PlaylistContext {
        override val playlistPermalink = "http://soundcloud.com/some-user/a-track?secret_token=thesecret"
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          TrackPermalinkUrl("some-user", "a-track", Some("thesecret"))
        )
      }

      "it does not URL percent decode already decoded urls" in new PlaylistContext {
        val playlistPermalinkDecodedUrl = "http://soundcloud.com/some-user/a-track"
        ResourceURLs.parsePermalinkUrl(playlistPermalinkDecodedUrl) ==== Success(
          TrackPermalinkUrl("some-user", "a-track", None)
        )
      }
    }

    "with playlist permalink" >> {
      trait PlaylistContext extends Context {
        val playlistPermalink = "http://soundcloud.com/some-user/sets/a:playlist"
      }

      "with NO secret token" in new PlaylistContext {
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          PlaylistPermalinkUrl("some-user", "a:playlist", None)
        )
      }

      "with a secret token as path param" in new PlaylistContext {
        override val playlistPermalink = "http://soundcloud.com/some-user/sets/a:playlist/thesecret"
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          PlaylistPermalinkUrl("some-user", "a:playlist", Some("thesecret"))
        )
      }

      "with a secret token as query param" in new PlaylistContext {
        override val playlistPermalink = "http://soundcloud.com/some-user/sets/a:playlist?secret_token=thesecret"
        ResourceURLs.parsePermalinkUrl(playlistPermalink) ==== Success(
          PlaylistPermalinkUrl("some-user", "a:playlist", Some("thesecret"))
        )
      }

      "it does not URL percent decode already decoded urls" in new PlaylistContext {
        val playlistPermalinkDecodedUrl = "http://soundcloud.com/some-user/sets/a%3Aplaylist"
        ResourceURLs.parsePermalinkUrl(playlistPermalinkDecodedUrl) ==== Success(
          PlaylistPermalinkUrl("some-user", "a:playlist", None)
        )
      }
    }

    "with unknown permalink" >> {
      trait UnknownContext extends Context {
        val unknownPermalink = "http://soundcloud.com/im/not/sure/what/this/is"
      }

      "it fails" in new UnknownContext {
        ResourceURLs.parsePermalinkUrl(unknownPermalink).toOption ==== None
      }
    }
  }
}
