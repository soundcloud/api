package com.soundcloud.publicApiStrangler.service

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.service.resolve.ResolveService
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.Future
import com.twitter.util.Await
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistBuilder
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import org.mockito.Mockito.verify
import com.soundcloud.publicApiStrangler.service.playlists.PlaylistRequest

class ResolveServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    val mockMoshimoshiClient = mock[MoshimoshiClient]
    val mockTrackVisibilityService = mock[TrackVisibilityService]
    val mockPlaylistsService = mock[PlaylistsService]
    val baseUrl = "http://base-url"
    val resolveService =
      new ResolveService(mockMoshimoshiClient, mockTrackVisibilityService, mockPlaylistsService, baseUrl)

    val sessionUser = Urn("soundcloud", "users", "1")
    val session = loggedInSession(sessionUser)
    val permalink: String

    def stubMoshimoshi(maybeUrn: Option[Urn]) = {
      mockMoshimoshiClient resolveToUrn (session, permalink) returns Future.value(maybeUrn)
    }
  }

  "#resolveUrl" >> {
    "users" >> {
      trait UsersContext extends Context {
        override val permalink: String = "https://soundcloud.com/user-885473394"
        val user = Urn("soundcloud", "users", "2")
      }

      "when moshimoshi resolves permalink to urn" >> {
        trait MoshimoshiSuccessContext extends UsersContext {
          stubMoshimoshi(Some(user))
        }

        "returns user url" in new MoshimoshiSuccessContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== Some(s"$baseUrl/users/2")
        }
      }

      "when moshimoshi does NOT resolve permalink to urn" >> {
        trait MoshimoshiSuccessContext extends UsersContext {
          stubMoshimoshi(None)
        }

        "returns None" in new MoshimoshiSuccessContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
      }
    }

    "tracks" >> {
      trait TracksContext extends Context with TrackRepresentationSpecContext {
        override val permalink: String = "https://soundcloud.com/remover/impact-moderato/s-0aQFV0COfSw"
      }

      "when moshimoshi resolves permalink to urn" >> {
        trait MoshimoshiSuccessContext extends TracksContext {
          stubMoshimoshi(Some(trackUrn))
        }

        "track is visible" >> {
          trait VisibleTrackContext extends MoshimoshiSuccessContext {
            mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List(defaultTrack))
          }

          "passes secret token to track service" in new VisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            val expectedRequest = TrackRequest(trackUrn, Some("s-0aQFV0COfSw"))
            verify(mockTrackVisibilityService).visibleTracks(any, ===(List(expectedRequest)), any, any)
          }

          "returns track url" in new VisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/tracks/1324?secret_token=s-0aQFV0COfSw")
          }
        }

        "track is NOT visible" >> {
          trait NotVisibleTrackContext extends MoshimoshiSuccessContext {
            mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List.empty)
          }

          "returns None" in new NotVisibleTrackContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== None
          }
        }

      }

      "when moshimoshi does not resolves permalink to urn" >> {
        trait MoshimoshiFailureContext extends TracksContext {
          stubMoshimoshi(None)
          mockTrackVisibilityService.visibleTracks(any, any, any, any) returns Future.value(List(defaultTrack))
        }

        "returns None" in new MoshimoshiFailureContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
      }
    }

    "playlists" >> {
      trait PlaylistsContext extends Context {
        override val permalink: String = "https://soundcloud.com/remover/sets/tortoise/s-I5aouttNwKq"
        val playlistUrn = Urn("soundcloud", "playlists", "1")
        val playlist = new PlaylistBuilder().build
      }

      "when moshimoshi resolves permalink to urn" >> {
        trait MoshimoshiSuccessContext extends PlaylistsContext {
          stubMoshimoshi(Some(playlistUrn))
        }

        "playlist is visible" >> {
          trait VisiblePlaylistContext extends MoshimoshiSuccessContext {
            mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List(playlist))
          }

          "passes secret token to playlist service" in new VisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            val expectedRequest = PlaylistRequest(playlistUrn, Some("s-I5aouttNwKq"))
            verify(mockPlaylistsService).fetchPlaylistsMetadataOnly(any, ===(List(expectedRequest)))
          }

          "returns playlist url" in new VisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== Some(s"$baseUrl/playlists/1?secret_token=s-I5aouttNwKq")
          }
        }

        "playlist is NOT visible" >> {
          trait NotVisiblePlaylistContext extends MoshimoshiSuccessContext {
            mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List.empty)
          }

          "returns None" in new NotVisiblePlaylistContext {
            val result = Await.result(resolveService.resolveUrl(session, permalink))
            result ==== None
          }
        }

      }

      "when moshimoshi does not resolves permalink to urn" >> {
        trait MoshimoshiFailureContext extends PlaylistsContext {
          stubMoshimoshi(None)
          mockPlaylistsService fetchPlaylistsMetadataOnly (any, any) returns Future.value(List(playlist))
        }

        "returns None" in new MoshimoshiFailureContext {
          val result = Await.result(resolveService.resolveUrl(session, permalink))
          result ==== None
        }
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
