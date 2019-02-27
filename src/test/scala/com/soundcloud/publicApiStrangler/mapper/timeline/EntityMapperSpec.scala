package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, LikesCount}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{Playlist, Track, User}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.{entityMapperLieblingLikesInfo, entityMapperOkidokiFetch}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mutable.Before
import play.api.libs.json.JsObject

class EntityMapperSpec extends UnitSpecification {

  trait Context extends Scope with Before {
    val userUrn = Urn("soundcloud", "users", "123")
    val okidokiClient = mock[OkidokiClient]
    val lieblingClient = mock[LieblingClient]
    val followCountsClient = mock[FollowCountsClient]
    val repostsClient = mock[RepostsClient]
    val entitySummaryMapper = mock[EntitySummaryMapper]
    lazy val entityMapper = new EntityMapper(okidokiClient,
      lieblingClient,
      followCountsClient,
      repostsClient,
      "https://foo.com",
      entitySummaryMapper)
    val session = new UserSessionBuilder().build()
    val trackUrn = Urn("soundcloud", "tracks", "131352352")
    val playlistUrn = Urn("soundcloud", "playlists", "123")
    val commentUrn = Urn("soundcloud", "comments", "205752728")
    val likeUrns = List(trackUrn, playlistUrn)
    val urns = likeUrns ++ List(userUrn, commentUrn)

    override def before: Any = {
      when(okidokiClient.fetch(===(session), any[Set[Urn]])) thenReturn
        Future(entityMapperOkidokiFetch.as[List[JsObject]])

      when(lieblingClient.likeCounts(===(session), ===(likeUrns))) thenReturn
        Future((entityMapperLieblingLikesInfo \ "likes_counts").as[List[LikesCount]])

      when(followCountsClient.counts(session, Seq(userUrn))) thenReturn
        Future.value(Seq(FollowCounts(userUrn, 1111, 2222)))

      when(repostsClient.getRepostCountsByUrnWithFallback(session, urns.toSet)) thenReturn
        Future.value(Map(userUrn -> 11L, trackUrn -> 22L, playlistUrn -> 33L))
    }

    def result = Await.result(entityMapper.materialize(session, urns))
  }

  "builds the proper mappings" in new Context {
    result.size mustEqual 4
  }

  "injects the base URL and likes and reposts counts" in new Context {
    val playlist: Playlist = result.filter(t => t.isInstanceOf[Playlist]).head.asInstanceOf[Playlist]

    playlist.tracks_uri mustEqual "https://foo.com/playlists/123/tracks"
    playlist.likes_count mustEqual Some(666)
    playlist.reposts_count mustEqual Some(33)
    val user: User = result.filter(t => t.isInstanceOf[User]).head.asInstanceOf[User]

    user.website_title mustEqual Some("Adeline Website")
    user.track_count mustEqual Some(49)
    user.followers_count ==== Some(1111)
    user.followings_count ==== Some(2222)
    user.reposts_count ==== Some(11)

    val track: Track = result.filter(t => t.isInstanceOf[Track]).head.asInstanceOf[Track]
    track.downloadable mustEqual Some(false) // downloadable respects `has_downloads_left`
    track.reposts_count mustEqual Some(22)
  }

}
