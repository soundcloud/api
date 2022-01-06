package com.soundcloud.apipublic.handler.comments

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.play.UrnFormat._

case class CreateCommentParams(trackUrn: Urn, body: String, timestamp: Option[Int], secretToken: Option[String])
object CreateCommentParams {
  implicit val reads = Json.reads[CreateCommentParams]
}
