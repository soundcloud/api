package com.soundcloud.apipublic.service.users

import play.api.libs.json.{Json, Writes}

case class UserUploadQuota(seconds_used: Int, seconds_limit: Option[Int]) {
  def hasUnlimitedQuota: Boolean = seconds_limit.isEmpty
  def secondsLeft: Option[Int] = seconds_limit.map(_ - seconds_used)
}

object UserUploadQuota {
  implicit val reads = Json.reads[UserUploadQuota]

  implicit val writes: Writes[UserUploadQuota] = Writes { uploadQuota =>
    Json.obj(
      "unlimited_upload_quota" -> uploadQuota.hasUnlimitedQuota,
      "upload_seconds_used" -> uploadQuota.seconds_used,
      "upload_seconds_left" -> uploadQuota.secondsLeft
    )
  }
}
