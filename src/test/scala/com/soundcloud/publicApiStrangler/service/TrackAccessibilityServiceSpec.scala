package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.playlists.{Playlist, PlaylistsClient}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime

class TrackAccessibilityServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val playlistsClient = mock[PlaylistsClient]

    val service = new TrackAccessibilityService(playlistsClient)
  }

  "#areTracksAccessible" >> {

    "returns a value for every track urn" in new Context {
      val tracks = List(
        TrackMetadataTrackBuilder(public = true).build,
        TrackMetadataTrackBuilder(public = false).build)

      playlistsClient.getPlaylistContainingTrackOwnedByUser(tracks(0).urn, tracks(0).user_urn).returns(Future.value(List.empty))
      playlistsClient.getPlaylistContainingTrackOwnedByUser(tracks(1).urn, tracks(1).user_urn).returns(Future.value(List.empty))

      val result = Await.result(service.areTracksAccessible(anonymousSession, tracks))

      result ==== Map(
        tracks(0).urn -> true,
        tracks(1).urn -> false)
    }

  }

  "#isTrackAccessible" in new Context {
    val now = Some(DateTime.now)
    val user1 = Urn("soundcloud", "users", "1")
    val user2 = Urn("soundcloud", "users", "2")

    val secretToken1 = Some("secret1")
    val secretToken2 = Some("secret2")
    val playlist1 = Some(Playlist(user2, secretToken1.get))
    val playlist2 = Some(Playlist(user2, secretToken2.get))
    val playlist3 = Some(Playlist(user1, secretToken1.get))

    case class TestData(sessionUser: Option[Urn],
                        trackOwner: Urn,
                        public: Boolean,
                        disabledAt: Option[DateTime],
                        trackSecretToken: Option[String],
                        secretTokenInRequest: Option[String],
                        playlist: Option[Playlist],
                        shouldBeAllowed: Boolean) {
      override def toString = {
        s"""TestData(
           |sessionUser=$sessionUser
           |trackOwner=$trackOwner
           |public=$public
           |disabledAt=$disabledAt
           |trackSecretToken=$trackSecretToken
           |secretTokenInRequest=$secretTokenInRequest
           |playlist=$playlist
           |shouldBeAllowed=$shouldBeAllowed)
        """.stripMargin
      }
    }

    List(Option(1), None).flatten

    val testData = List(
      /**
        * disabled_at:
        * when the track is disabled, it doesn't matter if the track is public or the session user is the same as the track owner,
        * the track shouldn't be accessible.
        */
      TestData(Some(user1), user1, true, now, None, None, None, shouldBeAllowed = false),
      TestData(Some(user1), user2, true, now, None, None, None, shouldBeAllowed = false),

      /**
        * privacy:
        * - when the track's public, it should be accessible.
        * - when the track's private, it should be accessible if the session user is the same as the track owner.
        */
      TestData(Some(user1), user1, true, None, None, None, None, shouldBeAllowed = true),
      TestData(Some(user1), user2, true, None, None, None, None, shouldBeAllowed = true),
      TestData(Some(user1), user1, false, None, None, None, None, shouldBeAllowed = true),
      TestData(Some(user1), user2, false, None, None, None, None, shouldBeAllowed = false),

      /**
        * secret_token
        * when the track's private and the session user is different from the track owner, the track should only
        * be accessible if the sent secret_token matches the track's secret_token
        */
      TestData(Some(user1), user2, false, None, secretToken1, secretToken1, None, shouldBeAllowed = true),
      TestData(Some(user1), user2, false, None, None, secretToken2, None, shouldBeAllowed = false),
      TestData(Some(user1), user2, false, None, secretToken1, None, None, shouldBeAllowed = false),
      TestData(Some(user1), user2, false, None, secretToken1, secretToken2, None, shouldBeAllowed = false),

      /**
        * playlist secret_token
        * when the track's private and the session user is different form the track owner, and the sent
        * secret_token does not match the track's secret token, the track should only be accessible if:
        * - the track's inside a playlist owned by the track's owner
        * - the sent secret_token matches the playlist's secret_token
        */
      TestData(Some(user1), user2, false, None, None, secretToken1, playlist1, shouldBeAllowed = true),
      TestData(Some(user1), user2, false, None, None, secretToken2, playlist1, shouldBeAllowed = false),
      TestData(Some(user1), user2, false, None, None, secretToken1, playlist2, shouldBeAllowed = false),
      TestData(Some(user2), user1, false, None, None, secretToken1, playlist3, shouldBeAllowed = true),
      TestData(Some(user2), user1, false, None, None, secretToken2, playlist3, shouldBeAllowed = false)
    )

    testData.foreach(data => {
      val session = data.sessionUser match {
        case Some(user) => loggedInSession(user)
        case _ => anonymousSession
      }

      val track = TrackMetadataTrackBuilder(
        user_urn = data.trackOwner,
        public = data.public,
        secret_token = data.trackSecretToken.getOrElse(""),
        disabled_at = data.disabledAt).build

      val returnedPlaylists = data.playlist.map(List(_)).getOrElse(List.empty)
      playlistsClient.getPlaylistContainingTrackOwnedByUser(track.urn, track.user_urn)
        .returns(Future.value(returnedPlaylists))

      val result = Await.result(service.isTrackAccessible(session, data.secretTokenInRequest, track))
      result ==== data.shouldBeAllowed
    })
  }
}
