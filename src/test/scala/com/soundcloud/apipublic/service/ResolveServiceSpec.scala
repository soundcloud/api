package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.shortlinks.ShortLinksClient
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.service.resolve.{ResolveService, ResourceURLs}
import com.soundcloud.apipublic.client.profile.ProfilesClient
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.Future
import com.twitter.util.Await
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.soundcloud.apipublic.client.tracks.TrackRequest
import org.mockito.Mockito.verify
import com.soundcloud.apipublic.service.playlists.PlaylistRequest
import com.soundcloud.apipublic.service.TrackVisibilityService.TrackVisibilityFieldMask
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams

class ResolveServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val mockProfilesClient = mock[ProfilesClient]
    val shortLinksClient = mock[ShortLinksClient]
    val mockTrackVisibilityService = mock[TrackVisibilityService]
    val mockPlaylistsService = mock[PlaylistsService]
    val baseUrl = "http://base-url"
    val resolveService =
      new ResolveService(
        mockProfilesClient,
        shortLinksClient,
        mockTrackVisibilityService,
        mockPlaylistsService,
        baseUrl
      )

    val sessionUser = Urn("soundcloud", "users", "1")
    val session = loggedInSession(sessionUser)
    def permalink: String

    def stubProfilesResolve(maybeUrn: Option[Urn]) = {
      val normalised = ResourceURLs.parsePermalinkUrl(permalink).toOption.get.normalized
      mockProfilesClient.resolvePermalink(any, ===(normalised)) returns Future.value(maybeUrn)
    }

    def stubShortLinksClient(maybeRedirectUrl: Option[String]) = {
      shortLinksClient.resolveUrl(any) returns Future.value(maybeRedirectUrl)
    }
  }

  "#resolveUrl" >> {
    "users" >> {
      trait UsersContext extends Context {
        override def permalink: String = "https://soundcloud.com/user-885473394"
        val user = Urn("soundcloud", "users", "2")
      }

      "when profiles resolves permalink to urn" >> {
        trait ProfilesSuccessContext extends UsersContext {
          stubProfilesResolve(Some(user))
        }

        "returns user url" in new ProfilesSuccessContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== Some(s"$baseUrl/users/soundcloud:users:2")
        }

        "preserves whitelisted query params" in new ProfilesSuccessContext {
          override def permalink =
            "https://soundcloud.com/user-885473394?client_id=the-client-id&other=should-not-be-included"
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== Some(s"$baseUrl/users/soundcloud:users:2?client_id=the-client-id")
        }

        "no query params returned when permalink does not include any of the whitelisted params" in new ProfilesSuccessContext {
          override def permalink = "https://soundcloud.com/user-885473394?&other=should-not-be-included"
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== Some(s"$baseUrl/users/soundcloud:users:2")
        }
      }

      "when profiles does NOT resolve permalink to urn" >> {
        trait ProfilesSuccessContext extends UsersContext {
          stubProfilesResolve(None)
        }

        "returns None" in new ProfilesSuccessContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
      }
    }

    "tracks" >> {
      trait TracksContext extends Context with TrackRepresentationSpecContext {
        override def permalink: String = "https://soundcloud.com/remover/impact-moderato/s-0aQFV0COfSw"
      }

      "when profiles resolves permalink to urn" >> {
        trait ProfilesSuccessContext extends TracksContext {
          stubProfilesResolve(Some(trackUrn))
        }

        "track is visible" >> {
          trait VisibleTrackContext extends ProfilesSuccessContext {
            mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List(defaultTrack))
          }

          "passes secret token to track service" in new VisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            val expectedRequest = TrackRequest(trackUrn, Some("s-0aQFV0COfSw"))
            verify(mockTrackVisibilityService).visibleTracks(
              any,
              ===(List(expectedRequest)),
              ===(TrackVisibilityFieldMask),
              ===(AccessParams.explicitAccess)
            )
          }

          "returns track url" in new VisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/tracks/soundcloud:tracks:1324?secret_token=s-0aQFV0COfSw")
          }

          "preserves whitelisted query params" in new VisibleTrackContext {
            override def permalink =
              "https://soundcloud.com/tracks/soundcloud:tracks:1324?client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/tracks/soundcloud:tracks:1324?client_id=the-client-id")
          }

          "with secret token as path component, preserves secret token as well as whitelisted query params" in new VisibleTrackContext {
            override def permalink =
              "https://soundcloud.com/remover/impact-moderato/s-0aQFV0COfSw?client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(
              s"$baseUrl/tracks/soundcloud:tracks:1324?client_id=the-client-id&secret_token=s-0aQFV0COfSw"
            )
          }

          "with secret token as query param, preserves secret token as well as whitelisted query params" in new VisibleTrackContext {
            override def permalink =
              "http://soundcloud.com/remover/impact-moderato?secret_token=thesecret&client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/tracks/soundcloud:tracks:1324?client_id=the-client-id&secret_token=thesecret")
          }

          "no query params returned when permalink does not include any of the whitelisted params" in new VisibleTrackContext {
            override def permalink = "https://soundcloud.com/tracks/1324?&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/tracks/soundcloud:tracks:1324")
          }
        }

        "track is NOT visible" >> {
          trait NotVisibleTrackContext extends ProfilesSuccessContext {
            mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List.empty)
          }

          "returns None" in new NotVisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== None
          }
        }

      }

      "when profiles does not resolve permalink to urn" >> {
        trait ProfilesFailureContext extends TracksContext {
          stubProfilesResolve(None)
          mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List(defaultTrack))
        }

        "returns None" in new ProfilesFailureContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
      }
    }

    "playlists" >> {
      trait PlaylistsContext extends Context {
        override def permalink: String = "https://soundcloud.com/remover/sets/tortoise/s-I5aouttNwKq"
        val playlistUrn = Urn("soundcloud", "playlists", "1")
        val playlist = new PlaylistBuilder().build
      }

      "when profiles resolves permalink to urn" >> {
        trait ProfilesSuccessContext extends PlaylistsContext {
          stubProfilesResolve(Some(playlistUrn))
        }

        "playlist is visible" >> {
          trait VisiblePlaylistContext extends ProfilesSuccessContext {
            mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List(playlist))
          }

          "passes secret token to playlist service" in new VisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            val expectedRequest = PlaylistRequest(playlistUrn, Some("s-I5aouttNwKq"))
            verify(mockPlaylistsService).fetchPlaylistsMetadataOnly(any, ===(List(expectedRequest)))
          }

          "returns playlist url" in new VisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/playlists/soundcloud:playlists:1?secret_token=s-I5aouttNwKq")
          }

          "preserves whitelisted query params" in new VisiblePlaylistContext {
            override def permalink =
              "https://soundcloud.com/playlists/soundcloud:playlists:1?client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/playlists/soundcloud:playlists:1?client_id=the-client-id")
          }

          "with secret token as path component, preserves secret token as well as whitelisted query params" in new VisiblePlaylistContext {
            override def permalink =
              "https://soundcloud.com/remover/sets/tortoise/s-I5aouttNwKq?client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(
              s"$baseUrl/playlists/soundcloud:playlists:1?client_id=the-client-id&secret_token=s-I5aouttNwKq"
            )
          }

          "with secret token as query param, preserves secret token as well as whitelisted query params" in new VisiblePlaylistContext {
            override def permalink =
              "http://soundcloud.com/some-user/sets/a:playlist?secret_token=thesecret&client_id=the-client-id&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(
              s"$baseUrl/playlists/soundcloud:playlists:1?client_id=the-client-id&secret_token=thesecret"
            )
          }

          "no query params returned when permalink does not include any of the whitelisted params" in new VisiblePlaylistContext {
            override def permalink = "https://soundcloud.com/playlists/1?&other=should-not-be-included"
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/playlists/soundcloud:playlists:1")
          }
        }

        "playlist is NOT visible" >> {
          trait NotVisiblePlaylistContext extends ProfilesSuccessContext {
            mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List.empty)
          }

          "returns None" in new NotVisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== None
          }
        }

      }

      "when profiles does not resolve permalink to urn" >> {
        trait ProfilesFailureContext extends PlaylistsContext {
          stubProfilesResolve(None)
          mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List(playlist))
        }

        "returns None" in new ProfilesFailureContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
      }
    }

    "firebase permalinks" >> {
      "if the shortlink is valid" in new Context {
        val redirectPermalink = "https://on.soundcloud.com/abcdefg"
        override def permalink = "https://soundcloud.com/user-2-permalink"
        stubShortLinksClient(Some(permalink))
        stubProfilesResolve(Some(Urn("soundcloud", "users", "2")))
        Await.result(resolveService.resolveUrl(session, redirectPermalink)) ==== Some(
          s"$baseUrl/users/soundcloud:users:2"
        )
      }
      "if the shortlink is invalid" in new Context {
        val redirectPermalink = "https://on.soundcloud.com/invalid_shortlink"
        override def permalink = "no permalink"
        stubShortLinksClient(None)
        Await.result(resolveService.resolveUrl(session, redirectPermalink)) ==== None
      }
    }

    "invalid permalinks" >> {
      "invalid track permalink returns None" in new Context {
        override val permalink = "https://soundcloud.com/my/great/track/go/me"
        Await.result(resolveService.resolveUrl(session, permalink)) ==== None
      }

      "invalid user permalink returns None" in new Context {
        override val permalink = "https://m.soundcloud.com/user-885473394"
        Await.result(resolveService.resolveUrl(session, permalink)) ==== None
      }

      "invalid playlist permalink returns None" in new Context {
        override val permalink = "https://soundcloud.com/some-user/badstuffs/sets/something"
        Await.result(resolveService.resolveUrl(session, permalink)) ==== None
      }
    }
  }
}
