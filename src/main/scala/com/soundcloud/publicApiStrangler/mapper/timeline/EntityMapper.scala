package com.soundcloud.publicApiStrangler.mapper.timeline

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.mapping.timeline.{Comment, Playlist, Track, User}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.{LieblingClient, OkidokiClient}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient,
                   lieblingClient: LieblingClient,
                   baseUrl: String,
                   entitySummaryMapper: EntitySummaryMapper)
  extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val likes = lieblingClient.likesCounts(session, inputs.toList)
    val entities = okidokiClient.fetch(session, inputs)

    entities.join(likes).map {
      case (entities: List[JsObject], likesInfo: JsObject) =>
        val likesCounts = likeCounts(likesInfo)
        entities.map {
          entity =>
            val urn = Urn((entity \ "self" \ "urn").as[String])
            urn -> entityFor(urn, entity, likesCounts)
        }
    }.map(_.toMap)
  }


  private def entityFor(urn: Urn, entityData: JsObject, likesCounts: Map[Urn, Int])(implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new User(entityData, baseUrl, None)
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
