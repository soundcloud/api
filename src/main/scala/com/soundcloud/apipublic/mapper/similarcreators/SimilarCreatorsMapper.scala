package com.soundcloud.apipublic.mapper.similarcreators

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{JsError, JsObject, JsSuccess, JsValue}

object SimilarCreatorsMapper {
  def apply(json: JsValue): SimilarCreators = {
    val urns = (json \ "users").validate[List[JsObject]] match {
      case JsSuccess(objs, _) =>
        objs.flatMap { obj =>
          (obj \ "urn").validate[Urn] match {
            case JsSuccess(u, _) => Some(u)
            case _: JsError => None
          }
        }
      case _: JsError => Nil
    }
    SimilarCreators(urns)
  }
}
