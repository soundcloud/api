package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.mapping.timeline.{CommentSummary, PlaylistSummary, TrackSummary, UserSummary}
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntitySummaryMapper(okidokiClient: OkidokiClient,
                          repostsClient: RepostsClient,
                          enrichRepostsCounts: () => Future[Boolean],
                          baseUrl: String) extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    Future.join(
      okidokiClient.fetch(session, inputs),
      enrichRepostsCounts().flatMap {
        case true =>
          val justPlaylistUrns = inputs.filter(_.getCollection == "playlists")
          repostsClient.getRepostCountsByUrnWithFallback(session, justPlaylistUrns)
        case false =>
          Future.value(Map.empty[Urn,Long])
      }
    ).map { case (entities, playlistRepostCountsByUrn) =>
      entities.map { entity => {
        val urn = new Urn((entity \ "self" \ "urn").as[String])
        urn -> entityFor(urn, entity, playlistRepostCountsByUrn)
      }}
    }.map(_.toMap)
  }


   private def entityFor(urn: Urn, entityData: JsObject, playlistRepostCountsByUrn: Map[Urn,Long])(implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new UserSummary(entityData, baseUrl)
      case "tracks" => new TrackSummary(entityData, baseUrl, this)
      case "playlists" => new PlaylistSummary(entityData, playlistRepostCountsByUrn, baseUrl, this)
      case "comments" => new CommentSummary(entityData, baseUrl, this)
    }
  }

}
