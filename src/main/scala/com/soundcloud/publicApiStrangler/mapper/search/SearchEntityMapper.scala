package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.support.mapping.InputValidation
import com.twitter.util.Future
import play.api.libs.json.JsObject

class SearchEntityMapper(
    okidokiClient: OkidokiClient,
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    baseUrl: String,
    likeCountMapper: LikeCountMapper,
    entitySummaryMapper: EntitySummaryMapper
) extends Mapper[Urn, JsonMapping]
    with InputValidation[Urn, JsonMapping] {
  override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(
      implicit context: MappingContext
  ): Future[Map[Urn, JsonMapping]] = {
    val userUrns = inputs.filter(_.collection == "users")

    for {
      (entities, followCountsMap, repostsCountsByUrn) <- Future.join(
        okidokiClient.fetch(session, inputs),
        followCountsClient
          .counts(session, userUrns.toList)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap),
        repostsClient.getRepostCountsByUrnWithFallback(session, inputs)
      )
    } yield {
      entities.map { entity =>
        val urn = (entity \ "self" \ "urn").as[Urn]
        urn -> entityFor(session, urn, entity, followCountsMap, repostsCountsByUrn)
      }.toMap
    }
  }

  private def entityFor(
      session: UserSession,
      urn: Urn,
      entityData: JsObject,
      followCountsMap: Map[Urn, FollowCounts],
      repostCountsByUrn: Map[Urn, Long]
  )(implicit context: MappingContext) = {
    urn.collection match {
      case "users" => new SearchUser(entityData, baseUrl, followCountsMap.get(urn), repostCountsByUrn.get(urn))
      case "playlists" =>
        new SearchPlaylist(entityData, likeCountMapper, repostCountsByUrn, baseUrl, entitySummaryMapper)
    }
  }
}
