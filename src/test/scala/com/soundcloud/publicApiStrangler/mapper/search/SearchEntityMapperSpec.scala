package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.bff.nextbff.mapping.{Mapping, MappingContext}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  MonetizationModel,
  Reason
}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserLikesCount}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Before
import play.api.libs.json.{JsObject, Json}

class SearchEntityMapperSpec extends UnitSpecification {
  trait Context extends Scope with Before {
    implicit val context = mock[MappingContext]
    val baseUrl = "https://api.soundcloud.com.com"
    val userUrn = Urn("soundcloud", "users", "1")
    val session = loggedInSession(userUrn)

    val okidokiClient = mock[OkidokiClient]
    val followCountsClient = mock[FollowCountsClient]
    val repostsClient = mock[RepostsClient]
    val lieblingClient = mock[LieblingClient]
    val likeCountMapper = new LikeCountMapper(lieblingClient)
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, repostsClient, baseUrl)
    val mapper = new SearchEntityMapper(
      okidokiClient,
      followCountsClient,
      repostsClient,
      baseUrl,
      likeCountMapper,
      entitySummaryMapper
    )

    val urns = List(
      Urn("soundcloud", "users", "2097360"),
      Urn("soundcloud", "playlists", "685235"),
      Urn("soundcloud", "groups", "30910")
    )

    val searchResults = urns

    val fetchedUserUrn = Urn("soundcloud", "users", "2097360")

    val playlistUrn = Urn("soundcloud", "playlists", "685235")
    val likableUrns = Set(playlistUrn)

    val authorizations = Seq(
      new ContentAuthorization(
        Urn("soundcloud", "tracks", "15273221"),
        ContentPolicy.ALLOW,
        Reason.UNKNOWN,
        MonetizationModel.NOT_APPLICABLE
      )
    )
    val okidokiFetch = contentsOf("okidoki", "search_fetch")
      .as[List[JsObject]]
    val lieblingLikesInfo = contentsOf("liebling", "search_likes_info")
      .as[UserLikesCount]

    override def before: Any = {
      // the main metadata fetch
      when(okidokiClient.fetch(session, searchResults.toSet)).thenReturn(
        Future(okidokiFetch)
      )
      // embedded entity summaries in tracks/playlists/groups metadata
      when(okidokiClient.fetch(session, urns.toSet.filter(_.collection == "users"))).thenReturn(
        Future(okidokiFetch.filter(json => (json \ "self" \ "urn").as[Urn].collection == "users"))
      )

      // like counts / authenticated user likes
      val expected: PartialFunction[Seq[Urn], MatchResult[_]] = {
        case urns: Seq[Urn] => Set(urns: _*) === likableUrns
      }
      when(lieblingClient.userLikeCounts(===(session), beLike(expected), ===(userUrn), any[Int]))
        .thenReturn(Future(lieblingLikesInfo))

      // reposts_count enrichment
      when(repostsClient.getRepostCountsByUrnWithFallback(session, Set.empty)) thenReturn Future.value(
        Map.empty[Urn, Long]
      )
      when(repostsClient.getRepostCountsByUrnWithFallback(session, searchResults.toSet)) thenReturn
        Future.value(Map(fetchedUserUrn -> 11L, playlistUrn -> 33L))

      followCountsClient.counts(session, Seq(fetchedUserUrn)) returns Future.value(
        Seq(FollowCounts(fetchedUserUrn, 1111, 2222))
      )
    }

    def result = Await.result(mapper.materialize(session, searchResults))

    def mappingToJsObject(m: Mapping): JsObject = Json.parse(UntypedJson.write(m)).as[JsObject]
  }

  "builds the proper mappings" >> {

    "with follow counts" in new Context {
      result.size mustEqual 2
      val List(userJson, playlistJson) = result.map(mappingToJsObject)

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
      result.size mustEqual 2
      val List(userJson, playlistJson) = result.map(mappingToJsObject)

      (userJson \ "kind").as[String] ==== "user"
      (userJson \ "id").as[Int].toString ==== fetchedUserUrn.identifier
      (userJson \ "reposts_count").asOpt[Long] ==== Some(11L)

      (playlistJson \ "kind").as[String] ==== "playlist"
      (playlistJson \ "reposts_count").asOpt[Long] ==== Some(33L)
    }
  }
}
