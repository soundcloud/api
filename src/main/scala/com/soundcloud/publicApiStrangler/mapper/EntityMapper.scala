package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.mapping.{Playlist, Track, User}
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit._
import com.soundcloud.service.client.{LieblingClient, OkidokiClient, MoshimoshiClient}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient, lieblingClient: LieblingClient, baseUrl: String) extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    val likes = lieblingClient.likesCounts(session, inputs.toList)
    val entities = okidokiClient.fetch(session, inputs.toList)

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
      case "users" => new User(entityData, baseUrl)
      case "tracks" => new Track(entityData, likesCounts, this)
      case "playlists" => new Playlist(entityData, likesCounts, baseUrl, this)
    }
  }

  private def likeCounts(likesInfo: JsObject): Map[Urn, Int] = {
    (likesInfo \  "likes_counts").as[Seq[JsObject]].map {
      obj =>
        Urn((obj \ "target_urn").as[String]) -> (obj \ "likes_count").asOpt[Int].getOrElse(0)
    }.toMap.withDefaultValue(0)
  }

}
