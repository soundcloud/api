package com.soundcloud.publicApiStrangler.client.chrono

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class ChronoItem(timestamp: String, itemType: String, urn: Urn, cursor: String)

case class ChronoMetaParams(cursor: Option[String], limit: Int, direction: String)

case class ChronoMeta(params: ChronoMetaParams, validForCaching: Boolean)

case class ChronoResponse(items: List[ChronoItem], meta: ChronoMeta)

object ChronoItem {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val reads: Reads[ChronoItem] = Reads { json =>
    try {
      JsSuccess(
        ChronoItem(
          (json \ "timestamp").as[String],
          (json \ "type").as[String],
          (json \ "urn").as[Urn],
          (json \ "cursor").as[String]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}

object ChronoMetaParams {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val reads: Reads[ChronoMetaParams] = Json.reads[ChronoMetaParams]
}

object ChronoMeta {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val reads: Reads[ChronoMeta] = Json.reads[ChronoMeta]
}

object ChronoResponse {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val reads: Reads[ChronoResponse] = Json.reads[ChronoResponse]

  def emptyResponse: ChronoResponse = ChronoResponse(
    items = List.empty,
    meta = ChronoMeta(
      params = ChronoMetaParams(
        cursor = None,
        limit = 0,
        direction = "desc"
      ),
      validForCaching = false
    )
  )
}
