package com.soundcloud.publicApiStrangler.client.liebling

import com.soundcloud.jvmkit.module.util.Urn
import org.joda.time.DateTime
import play.api.libs.json._
import com.soundcloud.jvmkit.module.json.UrnFormat._

object Like {
  val ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ"
  implicit val readsDateTime: Reads[DateTime] = JodaReads.jodaDateReads(ISO8601)
  implicit val writesDateTime: Writes[DateTime] = JodaWrites.jodaDateWrites(ISO8601)

  implicit val format: Format[Like] = Json.format[Like]
}

case class Like(user_urn: Urn, target_urn: Urn, created: DateTime, deleted_at: Option[DateTime])

case class LikesPage(likes: List[Like], meta: LikesPageMeta)

case class LikesPageMeta(cursor: LikesPageCursor)

case class LikesPageCursor(next_params: Option[LikesPageNextParams], next_href: Option[String])

case class LikesPageNextParams(cursor: String, page_size: Int)

object LikesPage {
  implicit val format: Format[LikesPage] = Json.format[LikesPage]
}

object LikesPageMeta {
  implicit val format: Format[LikesPageMeta] = Json.format[LikesPageMeta]
}

object LikesPageCursor {
  implicit val format: Format[LikesPageCursor] = Json.format[LikesPageCursor]
}

object LikesPageNextParams {
  implicit val format: Format[LikesPageNextParams] = Json.format[LikesPageNextParams]
}
