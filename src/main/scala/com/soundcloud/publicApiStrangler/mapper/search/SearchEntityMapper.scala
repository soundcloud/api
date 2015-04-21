package com.soundcloud.publicApiStrangler.mapper.search

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.jvmkit.policies.ContentAuthorization
import com.soundcloud.publicApiStrangler.mapper.liebling.LikeCountMapper
import com.soundcloud.publicApiStrangler.mapper.purchaselink.TrackPurchaseLinkMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import com.soundcloud.publicApiStrangler.mapper.waveform.WaveformMapper
import com.soundcloud.publicApiStrangler.mapping.search._
import com.soundcloud.publicApiStrangler.support.mapping.InputValidation
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.Future
import play.api.libs.json.JsObject

class SearchEntityMapper(okidokiClient: OkidokiClient,
                         baseUrl: String,
                         contentAuthorization: ContentAuthorizationService,
                         waveform: WaveformMapper,
                         trackPurchaseLinkMapper: TrackPurchaseLinkMapper,
                         likeCountMapper: LikeCountMapper,
                         playlistTracksMapper: PlaylistTracksMapper,
                         entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping]
  with InputValidation[Urn, JsonMapping] {

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val trackUrns = inputs.filter(_.getCollection == "tracks")
    val authorizations = contentAuthorization.findRulesApplicableTo(session, trackUrns.toSeq).map(authorizationsByUrn)
    val entities = okidokiClient.fetch(session, inputs)
    Future.join(entities, authorizations).map {
      case (entities: List[JsObject], authorizations: Map[Urn, ContentAuthorization]) =>
        entities.map {
          entity =>
            val urn = Urn((entity \ "self" \ "urn").as[String])
            urn -> entityFor(session, urn, entity, authorizations)
        }
    }.map(_.toMap)
  }


  private def entityFor(session: UserSession, urn: Urn, entityData: JsObject, contentAuthorization: Map[Urn, ContentAuthorization])
                       (implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new SearchUser(entityData, baseUrl)
      case "tracks" => new SearchTrack(session, entityData, likeCountMapper, baseUrl, entitySummaryMapper, contentAuthorization(urn), waveform, trackPurchaseLinkMapper)
      case "playlists" => new SearchPlaylist(entityData, likeCountMapper, baseUrl,playlistTracksMapper, entitySummaryMapper)
      case "groups" => new SearchGroup(entityData, baseUrl, entitySummaryMapper)
    }
  }

  private def authorizationsByUrn(rules: Seq[ContentAuthorization]): Map[Urn, ContentAuthorization] =
    rules.map(rule => rule.getUrn -> rule).toMap

}



