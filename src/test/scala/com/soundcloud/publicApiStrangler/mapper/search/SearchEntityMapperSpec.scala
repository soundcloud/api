package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.Json
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.{TrackWaveformUrlMapper, WaveformUrlsRepository}
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, Reason}
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.{LieblingClient, OkidokiClient}
import com.soundcloud.service.response.representation.{TrackMeta, TracksWithPagination, TrackPurchaseLink}
import com.soundcloud.service.response.representation.liebling.UserLikesCount
import com.twitter.util.{Await, Future}
import org.specs2.matcher.MatchResult
import play.api.libs.json.{JsValue, JsObject}

class SearchEntityMapperSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    implicit val context = mock[MappingContext]
    val baseUrl = "https://api.soundcloud.com.com"
    val userUrn = Urn("soundcloud:users:1")
    val session = loggedInSession(userUrn)

    val okidokiClient = mock[OkidokiClient]
    val contentAuthorizationService = mock[ContentAuthorizationService]
    val trackPurchaseLinkMapper = new TrackPurchaseLinkMapper(okidokiClient)
    val lieblingClient = mock[LieblingClient]
    val likeCountMapper = new LikeCountMapper(lieblingClient)
    val waveformUrlsRepository = mock[WaveformUrlsRepository]
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, baseUrl)
    private val playlistTracksMapper = new PlaylistTracksMapper(okidokiClient, baseUrl)
    val mapper = new SearchEntityMapper(
      okidokiClient,
      baseUrl,
      contentAuthorizationService,
      new WaveformMapper(waveformUrlsRepository),
      trackPurchaseLinkMapper,
      likeCountMapper,
      playlistTracksMapper,
      entitySummaryMapper
    )

    val urns = List(
      "soundcloud:users:2097360",
      "soundcloud:tracks:15273221",
      "soundcloud:playlists:685235",
      "soundcloud:groups:30910"
    ).map(Urn(_))

    val likableUrns = Set(
      "soundcloud:tracks:15273221",
      "soundcloud:playlists:685235"
    ).map(Urn(_))


    val authorizations = Seq(
      new ContentAuthorization(Urn("soundcloud:tracks:15273221"), ContentPolicy.ALLOW, Reason.UNKNOWN)
    )
    val okidokiFetch = withContentsOf("okidoki", "search_fetch")
      .as[List[JsObject]]
    val lieblingLikesInfo = withContentsOf("liebling", "search_likes_info")
      .as[UserLikesCount]
    val waveforms = (new TrackWaveformUrlMapper).map(withContentsOf("waveform", "search_waveform"))
      .filterNot(_.isPreview)

    override def before = {
      // the main metadata fetch
      when(okidokiClient.fetch(===(session), ===(urns.toSet))).thenReturn(
        Future(okidokiFetch)
      )
      // embedded entity summaries in tracks/playlists/groups metadata
      when(okidokiClient.fetch(===(session), ===(urns.toSet.filter(_.getCollection == "users")))).thenReturn(
        Future(okidokiFetch.filter(json => Urn((json \ "self" \ "urn").as[String]).getCollection == "users"))
      )
      // purchase links in tracks metadata
      when(okidokiClient.trackPurchaseLinks(session, urns.filter(_.getCollection == "tracks").toSet)).thenReturn(
        Future(List(TrackPurchaseLink(Urn("soundcloud:tracks:15273221"), Some("itunes"), "http://example.org")))
      )
      // track metadata for a playlist -- one call per playlist :(
      // should probably return some non-empty list
      when(okidokiClient.playlistTracks(===(session), ===(Urn("soundcloud:playlists:685235")), any[Option[Int]], any[Option[Int]])).thenReturn(
        Future(TracksWithPagination(Nil, TrackMeta(None)))
      )
      
      // like counts / authenticated user likes
      val expected: PartialFunction[Seq[Urn], MatchResult[_]] =  { case urns: Seq[Urn] => Set(urns: _*) === likableUrns}
      when(lieblingClient.userLikeCounts(===(session), beLike(expected), ===(userUrn), any[Int]))
        .thenReturn(Future(lieblingLikesInfo))

      // waveform URLs
      when(contentAuthorizationService.findRulesApplicableTo(===(session), any[Seq[Urn]])).thenReturn(
        Future(authorizations)
      )
      when(waveformUrlsRepository.fetchWaveformUrlsToMap(===(session), any[Map[String, ContentPolicy]])).thenReturn(
        Future(waveforms.map(w => w.trackUid -> w).toMap)
      )
      
    }

    def result = Await.result(mapper.materialize(session, urns))
  }

  "builds the proper mappings" in new Context {
    result.size mustEqual 4
    val List(userJson, trackJson, playlistJson, groupJson) = result.map(Json.toJsValue)

    (trackJson \ "kind").as[String] ==== "track"
    (trackJson \ "waveform_url").as[String] ==== "https://w1.sndcdn.com/b5uH7mT3hjkm_m.png"
    (trackJson \ "duration").asOpt[Int] ==== Some(269555)
    (trackJson \ "download_url").asOpt[String] ==== Some("https://api.soundcloud.com/tracks/15273221/download")

    (userJson \ "kind").as[String] ==== "user"
    val subs = (userJson \ "subscriptions").as[List[JsObject]]
    subs must haveSize(1)
    (subs.head \ "product" \ "id").as[String] ==== "creator-pro-unlimited"

    (groupJson \ "kind").as[String] ==== "group"
    (groupJson \ "uri").as[String] ==== "https://api.soundcloud.com.com/groups/30910"

    (playlistJson \ "kind").as[String] ==== "playlist"
    (playlistJson \ "tracks_uri").as[String] ==== "https://api.soundcloud.com.com/playlists/685235/tracks"
    (playlistJson \ "likes_count").as[Int] ==== 666
    (playlistJson \ "tracks").as[List[JsValue]] must beEmpty
    (playlistJson \ "secret_token").asOpt[String] ==== None
    (playlistJson \ "secret_uri").asOpt[String] ==== None
  }
}
