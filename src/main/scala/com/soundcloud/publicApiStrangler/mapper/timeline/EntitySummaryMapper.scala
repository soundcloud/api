package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.timeline.{CommentSummary, PlaylistSummary, TrackSummary, UserSummary}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.OkidokiClient
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntitySummaryMapper(okidokiClient: OkidokiClient, baseUrl: String) extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    okidokiClient.fetch(session, inputs).map {
      entities: List[JsObject] =>
        entities.map {
          entity =>
            val urn = Urn((entity \ "self" \ "urn").as[String])
            urn -> entityFor(urn, entity)
        }
    }.map(_.toMap)
  }


  private def entityFor(urn: Urn, entityData: JsObject)(implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new UserSummary(entityData, baseUrl)
      case "tracks" => new TrackSummary(entityData, baseUrl, this)
      case "playlists" => new PlaylistSummary(entityData, baseUrl, this)
      case "comments" => new CommentSummary(entityData, baseUrl)
    }
  }

}
