package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.timeline._
import com.soundcloud.scalakit._
import com.soundcloud.service.client.{LieblingClient, OkidokiClient}
import com.soundcloud.service.response.representation.liebling.LikesCount
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient,
                   lieblingClient: LieblingClient,
                   baseUrl: String,
                   entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val likesCountsF = lieblingClient.likeCounts(session, inputs.toList)
    val entitiesF = okidokiClient.fetch(session, inputs)

    entitiesF.join(likesCountsF).map { case (entities, likesCounts) =>
      entities.map { entity =>
        val urn = Urn((entity \ "self" \ "urn").as[String])
        urn -> entityFor(urn, entity, likesCounts)
      }.toMap
    }
  }

  private def entityFor(urn: Urn, entityData: JsObject, likesCounts: Seq[LikesCount])
                       (implicit context: MappingContext): JsonMapping with UrnSupport = {

    urn.getCollection match {
      case "users" => new User(entityData, baseUrl)
      case "tracks" => new Track(entityData, likesCounts, baseUrl, entitySummaryMapper)
      case "playlists" => new Playlist(entityData, likesCounts, baseUrl, entitySummaryMapper)
      case "comments" => new Comment(entityData, baseUrl, entitySummaryMapper)
    }
  }
}
