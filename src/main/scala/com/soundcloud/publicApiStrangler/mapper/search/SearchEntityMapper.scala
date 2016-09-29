package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.mapping.search._
import com.soundcloud.publicApiStrangler.support.mapping.InputValidation
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.Future
import play.api.libs.json.JsObject

class SearchEntityMapper(okidokiClient: OkidokiClient,
                         followCountsClient: FollowCountsClient,
                         baseUrl: String,
                         contentAuthorization: ContentAuthorizationRules,
                         waveform: WaveformMapper,
                         trackPurchaseLinkMapper: TrackPurchaseLinkMapper,
                         likeCountMapper: LikeCountMapper,
                         entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping]
  with InputValidation[Urn, JsonMapping] {

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val trackUrns = inputs.filter(_.getCollection == "tracks")
    val userUrns = inputs.filter(_.getCollection == "users")

    for {
      (entities, authorizations, followCountsMap) <- Future.join(
        okidokiClient.fetch(session, inputs),
        contentAuthorization.fetchRules(session, trackUrns.toSeq).map(authorizationsByUrn),
        followCountsClient
          .counts(session, userUrns.toList)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap)
      )
    } yield {
      entities.map {
        entity =>
          val urn = new Urn((entity \ "self" \ "urn").as[String])
          urn -> entityFor(session, urn, entity, authorizations, followCountsMap)
      }.toMap
    }
  }

  private def entityFor(session: UserSession,
                        urn: Urn, entityData: JsObject,
                        contentAuthorization: Map[Urn, ContentAuthorization],
                        followCountsMap: Map[Urn, FollowCounts])
                       (implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new SearchUser(entityData, baseUrl, followCountsMap.get(urn))
      case "tracks" => new SearchTrack(session, entityData, likeCountMapper, baseUrl, entitySummaryMapper, contentAuthorization(urn), waveform, trackPurchaseLinkMapper)
      case "playlists" => new SearchPlaylist(entityData, likeCountMapper, baseUrl, entitySummaryMapper)
      case "groups" => new SearchGroup(entityData, baseUrl, entitySummaryMapper)
    }
  }

  private def authorizationsByUrn(rules: Seq[ContentAuthorization]): Map[Urn, ContentAuthorization] =
    rules.map(rule => rule.getUrn -> rule).toMap

}
