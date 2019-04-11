package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{Comment, Playlist, Track, User}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient,
                   lieblingClient: LieblingClient,
                   followCountsClient: FollowCountsClient,
                   repostsClient: RepostsClient,
                   baseUrl: String,
                   entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    for {
      (entities, likes, followCountsMap, repostCountsByUrn) <- Future.join(
        okidokiClient.fetch(session, inputs),
        lieblingClient.likeCounts(session, filterByCollection(inputs.toList, List("tracks", "playlists"))),
        followCountsClient
          .counts(session, filterByCollection(inputs.toList, List("users")))
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap),
        repostsClient.getRepostCountsByUrnWithFallback(session, inputs)
      )
    } yield {
      val likesCounts = likes.map(like => like.target_urn -> like.likes_count).toMap
      entities.map {
        entity =>
          val urn = (entity \ "self" \ "urn").as[Urn]
          urn -> entityFor(urn, entity, likesCounts, followCountsMap, repostCountsByUrn)
      }.toMap
    }
  }

  private def filterByCollection(inputs: List[Urn], collections: List[String]): List[Urn] =
    inputs.filter(urn => collections.contains(urn.collection))

  private def entityFor(urn: Urn,
                        entityData: JsObject,
                        likesCounts: Map[Urn, Long],
                        followCountsMap: Map[Urn, FollowCounts],
                        repostCountsByUrn: Map[Urn, Long])
                       (implicit context: MappingContext) = {

    urn.collection match {
      case "users" => new User(entityData, baseUrl, followCountsMap.get(urn), repostCountsByUrn.get(urn))
      case "tracks" => new Track(entityData, likesCounts, repostCountsByUrn, baseUrl, entitySummaryMapper)
      case "playlists" => new Playlist(entityData, likesCounts, repostCountsByUrn, baseUrl, entitySummaryMapper)
      case "comments" => new Comment(entityData, baseUrl, entitySummaryMapper)
    }
  }

}
