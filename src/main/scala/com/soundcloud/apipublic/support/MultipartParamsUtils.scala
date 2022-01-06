package com.soundcloud.apipublic.support

import scala.util.matching.Regex

object MultipartParamsUtils {
  def extractFieldsFromParams(
      pattern: Regex,
      multipartParams: Map[String, Seq[String]]
  ): Map[String, String] = {
    multipartParams.foldLeft(Map[String, String]()) {
      case (acc, (key, value)) =>
        key match {
          case pattern(field) => acc + (field -> value.last)
          case _ => acc
        }
    }
  }
}
