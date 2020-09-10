package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.support.mapping.InputValidation
import com.twitter.util.Future

class SearchEntityMapper(
    okidokiClient: OkidokiClient,
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    baseUrl: String
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
        urn -> new SearchUser(entity, baseUrl, followCountsMap.get(urn), repostsCountsByUrn.get(urn))
      }.toMap
    }
  }
}
