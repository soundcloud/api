package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.media.{TrackWaveformUrlMapper, WaveformUrlsRepository}
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserLikesCount}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.json.UntypedJson
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.{TrackMeta, TracksWithPagination}
import com.twitter.util.{Await, Future}
import org.specs2.matcher.MatchResult
import play.api.libs.json.{JsObject, Json}
import org.mockito.Mockito.when
import org.specs2.mutable.Before

class SearchEntityMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends Scope with Before {
    implicit val context = mock[MappingContext]
    val baseUrl = "https://api.soundcloud.com.com"
    val userUrn = new Urn("soundcloud:users:1")
    val session = loggedInSession(userUrn)

    val okidokiClient = mock[OkidokiClient]
    val followCountsClient = mock[FollowCountsClient]
    val repostsClient = mock[RepostsClient]
    val contentAuthorizationService = mock[ContentAuthorizationRules]
    val lieblingClient = mock[LieblingClient]
    val likeCountMapper = new LikeCountMapper(lieblingClient)
    val waveformUrlsRepository = mock[WaveformUrlsRepository]
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, repostsClient, baseUrl)
    val mapper = new SearchEntityMapper(
      okidokiClient,
      followCountsClient,
      repostsClient,
      baseUrl,
      contentAuthorizationService,
      new WaveformMapper(waveformUrlsRepository),
      likeCountMapper,
      entitySummaryMapper
    )

    val urns = List(
      "soundcloud:users:2097360",
      "soundcloud:tracks:15273221",
      "soundcloud:playlists:685235",
      "soundcloud:groups:30910"
    ).map(new Urn(_))

    val searchResults = new Urn("soundcloud:tracks:-1") :: urns // doesn't exist in okidoki response

    val fetchedUserUrn = new Urn("soundcloud:users:2097360")

    val trackUrn = Urn("soundcloud:tracks:15273221")
    val playlistUrn = Urn("soundcloud:playlists:685235")
    val likableUrns = Set(trackUrn, playlistUrn)

    val authorizations = Seq(
      new ContentAuthorization(new Urn("soundcloud:tracks:15273221"), ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
    )
    val okidokiFetch = withContentsOf("okidoki", "search_fetch")
      .as[List[JsObject]]
    val lieblingLikesInfo = withContentsOf("liebling", "search_likes_info")
      .as[UserLikesCount]
    val waveforms = (new TrackWaveformUrlMapper).map(withContentsOf("waveform", "search_waveform"))
      .filterNot(_.isPreview)


    override def before: Any = {
      // the main metadata fetch
      when(okidokiClient.fetch(session, searchResults.toSet)).thenReturn(
        Future(okidokiFetch)
      )
      // embedded entity summaries in tracks/playlists/groups metadata
      when(okidokiClient.fetch(session, urns.toSet.filter(_.getCollection == "users"))).thenReturn(
        Future(okidokiFetch.filter(json => new Urn((json \ "self" \ "urn").as[String]).getCollection == "users"))
      )
      // track metadata for a playlist -- one call per playlist :(
      // should probably return some non-empty list
      when(okidokiClient.playlistTracks(===(session), ===(new Urn("soundcloud:playlists:685235")), any[Option[Int]], any[Option[Int]])).thenReturn(
        Future(TracksWithPagination(Nil, TrackMeta(None)))
      )

      // like counts / authenticated user likes
      val expected: PartialFunction[Seq[Urn], MatchResult[_]] = {
        case urns: Seq[Urn] => Set(urns: _*) === likableUrns
      }
      when(lieblingClient.userLikeCounts(===(session), beLike(expected), ===(userUrn), any[Int]))
        .thenReturn(Future(lieblingLikesInfo))

      // reposts_count enrichment
      when(repostsClient.getRepostCountsByUrnWithFallback(session, Set.empty)) thenReturn Future.value(Map.empty[Urn,Long])
      when(repostsClient.getRepostCountsByUrnWithFallback(session, searchResults.toSet)) thenReturn
        Future.value(Map(fetchedUserUrn -> 11L, trackUrn -> 22L, playlistUrn -> 33L))

      // waveform URLs
      when(contentAuthorizationService.fetchRules(===(session), any[Seq[Urn]])).thenReturn(Future.value(authorizations))
      when(waveformUrlsRepository.fetchWaveformUrlsToMap(===(session), any[Map[String, ContentPolicy]])).thenReturn(
        Future(waveforms.map(w => w.trackUid -> w).toMap)
      )
      followCountsClient.counts(session, Seq(fetchedUserUrn)) returns Future.value(Seq(FollowCounts(fetchedUserUrn, 1111, 2222)))
    }

    def result = Await.result(mapper.materialize(session, searchResults))

    def mappingToJsObject(m: Mapping): JsObject = Json.parse(UntypedJson.asString(m)).as[JsObject]
  }

  "builds the proper mappings" >> {
    "with purchase URL + title" in new Context {
      result.size mustEqual 3
      val List(userJson, trackJson, playlistJson) = result.map(mappingToJsObject _)

      (trackJson \ "purchase_url").as[String] ==== "http://example.com/store/music"
      (trackJson \ "purchase_title").as[String] ==== "i need the money ok"
    }

    "with follow counts" in new Context {
      result.size mustEqual 3
      val List(userJson, trackJson, playlistJson) = result.map(mappingToJsObject _)

      (trackJson \ "kind").as[String] ==== "track"
      (trackJson \ "waveform_url").as[String] ==== "https://w1.sndcdn.com/b5uH7mT3hjkm_m.png"
      (trackJson \ "duration").asOpt[Int] ==== Some(269555)
      (trackJson \ "streamable").asOpt[Boolean] ==== Some(false)
      (trackJson \ "downloadable").asOpt[Boolean] ==== Some(true)
      (trackJson \ "download_url").asOpt[String] ==== Some("https://api.soundcloud.com/tracks/15273221/download")

      (userJson \ "kind").as[String] ==== "user"
      val subs = (userJson \ "subscriptions").as[List[JsObject]]
      subs must haveSize(1)
      (subs.head \ "product" \ "id").as[String] ==== "creator-pro-unlimited"
      (userJson \ "followers_count").as[Long] ==== 1111
      (userJson \ "followings_count").as[Long] ==== 2222

      (playlistJson \ "kind").as[String] ==== "playlist"
      (playlistJson \ "tracks_uri").as[String] ==== "https://api.soundcloud.com.com/playlists/685235/tracks"
      (playlistJson \ "likes_count").as[Int] ==== 666
      (playlistJson \ "secret_token").asOpt[String] ==== None
      (playlistJson \ "secret_uri").asOpt[String] ==== None
    }

    "with enriched reposts_count fields" in new Context {
      result.size mustEqual 3
      val List(userJson, trackJson, playlistJson) = result.map(mappingToJsObject _)

      (userJson \ "kind").as[String] ==== "user"
      (userJson \ "id").as[Int].toString ==== fetchedUserUrn.getIdentifier
      (userJson \ "reposts_count").asOpt[Long] ==== Some(11L)

      (trackJson \ "kind").as[String] ==== "track"
      (trackJson \ "reposts_count").asOpt[Long] ==== Some(22L)

      (playlistJson \ "kind").as[String] ==== "playlist"
      (playlistJson \ "reposts_count").asOpt[Long] ==== Some(33L)
    }
  }
}
