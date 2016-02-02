package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.jvmkit.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapping.timeline.{Playlist, Track, User}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.{LieblingClient, OkidokiClient}
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsObject

class EntityMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val okidokiClient = mock[OkidokiClient]
    val lieblingClient = mock[LieblingClient]
    val followCountsClient = mock[FollowCountsClient]
    val getFollowCountsFromStitch: () => Future[Boolean]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    lazy val entityMapper = new EntityMapper(okidokiClient, lieblingClient, followCountsClient, "https://foo.com", entitySummaryMapper, getFollowCountsFromStitch)
    val session = mock[UserSession]
    val likeUrns = List(
      "soundcloud:tracks:131352352",
      "soundcloud:playlists:123"
    ).map(Urn(_))
    val urns = likeUrns ++ List(
      "soundcloud:users:123",
      "soundcloud:comments:205752728"
    ).map(Urn(_))

    override def before: Any = {
      when(okidokiClient.fetch(===(session), any[Set[Urn]])) thenReturn
        Future(okidokiFetch.as[List[JsObject]])

      when(lieblingClient.likesCounts(===(session), ===(likeUrns))) thenReturn
        Future(lieblingLikesInfo.as[JsObject])
    }

    def result = Await.result(entityMapper.materialize(session, urns))
  }

  "builds the proper mappings" in new Context {
    val getFollowCountsFromStitch = () => Future.False

    result.size mustEqual 4
  }

  "injects the base URL and likes counts" >> {
    "with follow counts flag off" in new Context {
      val getFollowCountsFromStitch = () => Future.False

      val playlist: Playlist = result.filter(t => t.isInstanceOf[Playlist]).head.asInstanceOf[Playlist]

      playlist.tracks_uri mustEqual "https://foo.com/playlists/123/tracks"
      playlist.likes_count mustEqual Some(666)
      val user: User = result.filter(t => t.isInstanceOf[User]).head.asInstanceOf[User]

      user.website_title mustEqual Some("Adeline Website")
      user.track_count mustEqual Some(49)
      user.followers_count ==== Some(20976)
      user.followings_count ==== Some(118)

      val track: Track = result.filter(t => t.isInstanceOf[Track]).head.asInstanceOf[Track]
      track.downloadable mustEqual Some(false) // downloadable respects `has_downloads_left`
    }

    "with follow counts flag on" in new Context {
      val getFollowCountsFromStitch = () => Future.True

      override def before: Any = {
        super.before
        val userUrn = Urn("soundcloud", "users", "123")
        when(followCountsClient.counts(session, Seq(userUrn))) thenReturn
          Future.value(Seq(FollowCounts(userUrn, 1111, 2222)))
      }

      val playlist: Playlist = result.filter(t => t.isInstanceOf[Playlist]).head.asInstanceOf[Playlist]

      playlist.tracks_uri mustEqual "https://foo.com/playlists/123/tracks"
      playlist.likes_count mustEqual Some(666)
      val user: User = result.filter(t => t.isInstanceOf[User]).head.asInstanceOf[User]

      user.website_title mustEqual Some("Adeline Website")
      user.track_count mustEqual Some(49)
      user.followers_count ==== Some(1111)
      user.followings_count ==== Some(2222)

      val track: Track = result.filter(t => t.isInstanceOf[Track]).head.asInstanceOf[Track]
      track.downloadable mustEqual Some(false) // downloadable respects `has_downloads_left`
    }
  }

}
