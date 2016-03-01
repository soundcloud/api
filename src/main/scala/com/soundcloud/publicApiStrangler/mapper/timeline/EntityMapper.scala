package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.mapping.timeline.{Comment, Playlist, Track, User}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.{LieblingClient, OkidokiClient}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient,
                   lieblingClient: LieblingClient,
                   followCountsClient: FollowCountsClient,
                   baseUrl: String,
                   entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    for {
      (entities, likes, followCountsMap) <- Future.join(
        okidokiClient.fetch(session, inputs),
        lieblingClient.likesCounts(session, filterByCollection(inputs.toList, List("tracks", "playlists"))),
        followCountsClient
          .counts(session, filterByCollection(inputs.toList, List("users")))
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap)
      )
    } yield {
      val likesCounts = likeCounts(likes)
      entities.map {
        entity =>
          val urn = Urn((entity \ "self" \ "urn").as[String])
          urn -> entityFor(urn, entity, likesCounts, followCountsMap)
      }.toMap
    }
  }

  private def filterByCollection(inputs: List[Urn], collections: List[String]): List[Urn] =
    inputs.filter(urn => collections.contains(urn.getCollection))

  private def entityFor(urn: Urn,
                        entityData: JsObject,
                        likesCounts: Map[Urn, Int],
                        followCountsMap: Map[Urn, FollowCounts])
                       (implicit context: MappingContext) = {
    
    urn.getCollection match {
      case "users" => new User(entityData, baseUrl, followCountsMap.get(urn))
      case "tracks" => new Track(entityData, likesCounts, baseUrl, entitySummaryMapper)
      case "playlists" => new Playlist(entityData, likesCounts, baseUrl, entitySummaryMapper)
      case "comments" => new Comment(entityData, baseUrl, entitySummaryMapper)
    }
  }

  private def likeCounts(likesInfo: JsObject): Map[Urn, Int] = {
    (likesInfo \  "likes_counts").as[Seq[JsObject]].map {
      obj =>
        Urn((obj \ "target_urn").as[String]) -> (obj \ "likes_count").asOpt[Int].getOrElse(0)
    }.toMap.withDefaultValue(0)
  }
}
