package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies._
import play.api.libs.json._
import com.soundcloud.publicApiStrangler.client.support.CommonJsonFormats.urnFormat

case class VisibleTrack(urn : Urn,
                        uid: Option[String],
                        apiStreamable: Option[Boolean],
                        authorization: ContentAuthorization)

object VisibleTrack {
  implicit val reads: Reads[VisibleTrack] = Reads { json =>
    try {
      JsSuccess(
        VisibleTrack(
          (json \ "urn").as[Urn],
          (json \ "uid").asOpt[String],
          (json \ "apiStreamable").asOpt[Boolean],
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


