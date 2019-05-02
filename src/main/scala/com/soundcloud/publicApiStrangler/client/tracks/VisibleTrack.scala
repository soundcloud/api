package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies._
import org.joda.time.DateTime
import play.api.libs.json.JodaReads._
import play.api.libs.json._

case class VisibleTrack(urn : Urn,
                        userUrn: Urn,
                        uid: Option[String],
                        apiStreamable: Option[Boolean],
                        downloadable: Boolean,
                        disabledAt: Option[DateTime],
                        authorization: ContentAuthorization)

object VisibleTrack {
  implicit val reads: Reads[VisibleTrack] = Reads { json =>
    try {
      JsSuccess(
        VisibleTrack(
          (json \ "urn").as[Urn],
          (json \ "userUrn").as[Urn],
          (json \ "uid").asOpt[String],
          (json \ "apiStreamable").asOpt[Boolean],
          (json \ "downloadable").as[Boolean],
          (json \ "disabled_at").asOpt[DateTime],
          new ContentAuthorization(
            (json \ "urn").as[Urn],
            ContentPolicy.from((json \ "authorization" \ "policy").as[String]),
            Reason.from((json \ "authorization" \ "reason").as[String]),
            MonetizationModel.from((json \ "authorization" \ "monetizationModel").as[String]),
          )
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}
