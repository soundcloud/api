package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{CommentSummary, PlaylistSummary, TrackSummary, UserSummary}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntitySummaryMapper(okidokiClient: OkidokiClient,
                          repostsClient: RepostsClient,
                          baseUrl: String) extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val justPlaylistUrns = inputs.filter(_.collection == "playlists")

    Future.join(
      okidokiClient.fetch(session, inputs),
      repostsClient.getRepostCountsByUrnWithFallback(session, justPlaylistUrns)
    ).map { case (entities, playlistRepostCountsByUrn) =>
      entities.map { entity =>
        val urn = (entity \ "self" \ "urn").as[Urn]
        urn -> entityFor(urn, entity, playlistRepostCountsByUrn)
      }
    }.map(_.toMap)
  }

  private def entityFor(urn: Urn, entityData: JsObject, playlistRepostCountsByUrn: Map[Urn, Long])(implicit context: MappingContext) = {
    urn.collection match {
      case "users" => new UserSummary(entityData, baseUrl)
      case "tracks" => new TrackSummary(entityData, baseUrl, this)
      case "playlists" => new PlaylistSummary(entityData, playlistRepostCountsByUrn, baseUrl, this)
      case "comments" => new CommentSummary(entityData, baseUrl, this)
    }
  }
}
