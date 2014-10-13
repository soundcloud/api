package com.soudcloud.publicApiStrangler.mapper

import com.soudcloud.publicApiStrangler.mapping.{Playlist, Track, User}
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit._
import com.soundcloud.service.client.{OkidokiClient, MoshimoshiClient}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class EntityMapper(okidokiClient: OkidokiClient) extends Mapper[Urn, JsonMapping] {

  override def map(session: UserSession, inputs: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, JsonMapping]] = {
    okidokiClient.fetch(session, inputs.toList).map(_.map {
        entity =>
          val urn = Urn((entity \ "self" \ "urn").as[String])
          urn -> entityFor(urn, entity)
      }
    ).map(_.toMap)
  }


  private def entityFor(urn: Urn, entityData: JsObject)(implicit context: MappingContext) = {
    urn.getCollection match {
      case "users" => new User(entityData)
      case "tracks" => new Track(entityData, this)
      case "playlists" => new Playlist(entityData, this)
    }
  }


}
